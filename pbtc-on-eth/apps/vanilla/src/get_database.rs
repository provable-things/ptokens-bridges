use std::{
    cell::RefCell,
    collections::HashMap,
};
use rocksdb::{
    DB,
    WriteBatch,
};
use crate::{
    constants::DATABASE_PATH,
    types::{
        Result,
        DataSensitivity,
    },
};
use ptokens_core::{
    traits::DatabaseInterface,
    errors::AppError as PbtcCoreError,
    types::{
        Bytes,
        Result as PbtcResult,
    },
};

pub struct Database {
    pub rocks_db: rocksdb::DB,
    pub keys_to_delete: RefCell<Vec<Bytes>>,
    pub hashmap: RefCell<HashMap<Bytes, Bytes>>,
}

impl Database {
    pub fn open() -> Result<Self> {
        Ok(
            Self {
                hashmap: RefCell::new(HashMap::new()),
                keys_to_delete: RefCell::new(Vec::new()),
                rocks_db: DB::open_default(DATABASE_PATH)?,
            }
        )
    }
}

impl DatabaseInterface for Database {
    fn end_transaction(&self) -> PbtcResult<()> {
        info!("✔ Ending DB transaction in app...");
        let mut batch = WriteBatch::default();
        trace!("✔ Adding keys & values from hashmap to batch DB writer...");
        self
            .hashmap
            .borrow_mut()
            .iter()
            .map(|(key, value)| batch.put(key, value))
            .for_each(drop);
        trace!("✔ Adding keys to delete to batch DB writer...");
        self
            .keys_to_delete
            .borrow_mut()
            .iter()
            .map(|key| batch.delete(key))
            .for_each(drop);
        trace!("✔ Batch writing to DB...");
        match self
            .rocks_db
            .write(batch) {
            Ok(_) => {
                trace!("✔ Atomic batch write successful!");
                Ok(())
            },
            Err(e) => {
                trace!("✘ Error batch writing to DB: {}", &e);
                Err(PbtcCoreError::Custom(e.to_string()))
            }
        }
    }

    fn start_transaction(&self) -> PbtcResult<()> {
        info!("✔ Starting DB transaction in app...");
        Ok(())
    }

    fn put(
        &self,
        key: Bytes,
        value: Bytes,
        _sensitivity: DataSensitivity,
    ) -> PbtcResult<()> {
        trace!("✔ Putting key in hashmap...");
        self
            .hashmap
            .borrow_mut()
            .insert(key, value);
        Ok(())
    }

    fn delete(&self, key: Bytes) -> PbtcResult<()> {
        trace!("✔ Removing key from hashmap...");
        self
            .hashmap
            .borrow_mut()
            .remove(&key);
        trace!("✔ Adding key to `to_delete` list...");
        self
            .keys_to_delete
            .borrow_mut()
            .push(key);
        Ok(())
    }

    fn get(
        &self,
        key: Bytes,
        _sensitivity: DataSensitivity
    ) -> PbtcResult<Bytes> {
        let not_in_db_error = "✘ Cannot find item in database!"
            .to_string();
        match self
            .keys_to_delete
            .borrow()
            .contains(&key) {
            true => {
                trace!("✔ Key already in delete list ∴ 'not found'!");
                Err(PbtcCoreError::Custom(not_in_db_error))
            }
            false => {
                trace!("✔ Checking hashmap for key...");
                match self
                    .hashmap
                    .borrow()
                    .get(&key) {
                    Some(value) => {
                        trace!("✔ Key found in hashmap!");
                        Ok(value.to_vec())
                    }
                    None => {
                        trace!("✘ Key NOT in hashmap!");
                        trace!("✔ Looking in underlying DB...");
                        match self.rocks_db.get(key) {
                            Ok(Some(value)) => {
                                trace!("✔ Key found in DB!");
                                Ok(value.to_vec())
                            }
                            Err(e) =>
                                Err(PbtcCoreError::Custom(e.to_string())),
                            Ok(None) =>
                                Err(PbtcCoreError::Custom(not_in_db_error))
                        }

                    }
                }
            }
        }
    }
}

pub fn get_database() -> Result<Database> {
    Database::open()
}
