use crate::types::{
    Result, 
    DataSensitivity
};

use ptokens_core::{
    traits::DatabaseInterface,
    errors::AppError as PbtcCoreError,
    types::{
        Bytes,
        Result as PbtcResult,
    }
};

use crate::{
    MAYBE_JNI_JAVAVM,
    CALLBACK_GLOBAL_REF
};

use jni::objects::{
    JObject, 
    JValue, 
};

pub struct Database {}

impl Database {
    pub fn open() -> Result<Self> {
        Ok(Self {})
    }
}

impl DatabaseInterface for Database {
    fn end_transaction(&self) -> PbtcResult<()> {
        info!("✔ End transaction");
        unsafe {
            match &MAYBE_JNI_JAVAVM {
                Some(javavm) => {
                    match &CALLBACK_GLOBAL_REF {
                        Some(callback_class_global_ref) => {
                            let env = javavm
                                .get_env()
                                .expect("✘ Error: CALLBACK_GLOBAL_REF is None!");
                            let callback_class = callback_class_global_ref
                                .as_obj();

                            let call_result = env.call_method(
                                callback_class, 
                                "endTransaction", 
                                "()V", 
                                &[]
                            );
                            match call_result {
                                Ok(_) => {
                                    info!("✔ Void result");
                                    Ok(())
                                },
                                Err(_) => Err(PbtcCoreError::Custom(
                                    "✘ Error: failed to call end_transaction() function!".to_string()
                                )),
                            }
                        },
                        None => Err(PbtcCoreError::Custom(
                            "✘ Error: CALLBACK_GLOBAL_REF is None!".to_string()
                        )),
                    }  
                },
                None => Err(PbtcCoreError::Custom(
                    "✘ Error: MAYBE_JNI_JAVAVM is None!".to_string()
                )),
            }
        }
    }

    fn start_transaction(&self) -> PbtcResult<()> {
        info!("✔ Start transaction");
        unsafe {
            match &MAYBE_JNI_JAVAVM {
                Some(javavm) => {
                    match &CALLBACK_GLOBAL_REF {
                        Some(callback_class_global_ref) => {
                            let env = javavm
                                .get_env()
                                .expect("✘ Error: CALLBACK_GLOBAL_REF is None!");
                            let callback_class = callback_class_global_ref
                                .as_obj();
                            let call_result = env.call_method(
                                callback_class, 
                                "startTransaction", 
                                "()V", 
                                &[]
                            );
                            match call_result {
                                Ok(_) => {
                                    info!("✔ Void result");
                                    Ok(())
                                },
                                Err(_) => Err(PbtcCoreError::Custom(
                                    "✘ Error: failed to call start_transaction() function!".to_string()
                                )),
                            }
                        },
                        None => Err(PbtcCoreError::Custom(
                            "✘ Error: CALLBACK_GLOBAL_REF is None!".to_string()
                        )),
                    }  
                },
                None => Err(PbtcCoreError::Custom(
                    "✘ Error: MAYBE_JNI_JAVAVM is None!".to_string()
                )),
            }
        }
    }

    fn put(
        &self, 
        key: Bytes, 
        value: Bytes, 
        data_sensitivity: DataSensitivity
    ) -> PbtcResult<()> {

        info!("✔ Putting bytes in database under key: {}", hex::encode(key.clone()));
        unsafe {
            match &MAYBE_JNI_JAVAVM {
                Some(javavm) => {
                    match &CALLBACK_GLOBAL_REF {
                        Some(callback_class_global_ref) => {
                            let env = javavm
                                .get_env()
                                .expect("✘ Error: CALLBACK_GLOBAL_REF is None!");
                            let callback_class = callback_class_global_ref
                                .as_obj();
                            let key_byte_array = env
                                .byte_array_from_slice(&key)
                                .expect("Invalid bytearray");
                            let value_byte_array = env
                                .byte_array_from_slice(&value)
                                .expect("Invalid bytearray");
                            let sensitivity = match data_sensitivity {
                                Some(s) => s,
                                None => 0,
                            };
                            let call_result = env.call_method(callback_class, "put", "([B[BB)V", &[ 
                                    JValue::from(JObject::from(key_byte_array)),
                                    JValue::from(JObject::from(value_byte_array)),
                                    JValue::from(sensitivity)
                            ]);
                            match call_result {
                                Ok(_) => {
                                    info!("✔ Value inserted");
                                    Ok(())
                                },
                                Err(_) => Err(PbtcCoreError::Custom(
                                    "✘ Error: failed to call put() function!".to_string()
                                )),
                            }
                        },
                        None => Err(PbtcCoreError::Custom(
                            "✘ Error: CALLBACK_GLOBAL_REF is None!".to_string()
                        )),
                    }  
                },
                None => Err(PbtcCoreError::Custom(
                    "✘ Error: MAYBE_JNI_JAVAVM is None!".to_string()
                )),
            }
        }
    }

    fn delete(
        &self, 
        key: Bytes,
    ) -> PbtcResult<()> {
    
        info!(
            "✔ Removing bytes from database under key: {}",
            hex::encode(key.clone())
        );

        unsafe {
            match &MAYBE_JNI_JAVAVM {
                Some(javavm) => {
                    match &CALLBACK_GLOBAL_REF {
                        Some(callback_class_global_ref) => {
                            let env = javavm
                                .get_env()
                                .expect("✘ Error: CALLBACK_GLOBAL_REF is None!");
                            let callback_class = callback_class_global_ref
                                .as_obj();
                            let key_byte_array = env
                                .byte_array_from_slice(&key)
                                .expect("Invalid bytearray");
                            let call_result = env.call_method(callback_class, "delete", "([B)V", &[ 
                                JValue::from(JObject::from(key_byte_array)) 
                            ]);
                            match call_result {
                                Ok(_) => {
                                    info!("✔ Value removed");
                                    Ok(())
                                },
                                Err(_) => Err(PbtcCoreError::Custom(
                                    "✘ Error: failed to call delete() function!".to_string()
                                )),
                            }
                        },
                        None => Err(PbtcCoreError::Custom(
                            "✘ Error: CALLBACK_GLOBAL_REF is None!".to_string()
                        )),
                    }  
                },
                None => Err(PbtcCoreError::Custom(
                    "✘ Error: MAYBE_JNI_JAVAVM is None!".to_string()
                )),
            }
        }
    }

    fn get(
        &self, 
        key: Bytes,
        data_sensitivity: DataSensitivity
    ) -> PbtcResult<Bytes> {
        info!("✔ Searching database for key: {}", hex::encode(&key));
        unsafe {
            match &MAYBE_JNI_JAVAVM {
                Some(javavm) => {
                    match &CALLBACK_GLOBAL_REF {
                        Some(callback_class_global_ref) => {
                            let env = javavm
                                .get_env()
                                .expect("✘ Error: CALLBACK_GLOBAL_REF is None!");
                            let callback_class = callback_class_global_ref
                                .as_obj();
                            let key_byte_array = env
                                .byte_array_from_slice(&key)
                                .expect("Invalid bytearray");
                            let sensitivity = match data_sensitivity {
                                Some(s) => s,
                                None => 0,
                            };

                            let call_result = env.call_method(callback_class, "get", "([BB)[B", &[ 
                                JValue::from(JObject::from(key_byte_array)),
                                JValue::from(sensitivity) 
                            ]).unwrap().l(); // l() --> get the returned object

                            match call_result {
                                Ok(res) => {
                                    match env.convert_byte_array(res.into_inner()) {
                                        Ok(ba) => {
                                            if ba.len() == 1 && ba[0] == 0x0F {
                                                info!("✘ Cannot find item in database!");
                                                Err(PbtcCoreError::Custom(
                                                    "✘ Cannot find item in database!".to_string()
                                                ))
                                            } else {
                                                info!("✔ Value obtained");
                                                Ok(ba)    
                                            }
                                        },
                                        Err(_) => Err(PbtcCoreError::Custom(
                                            "✘ Error: failed to convert to byte array".to_string()
                                        )),
                                    }
                                },
                                Err(_) => Err(PbtcCoreError::Custom(
                                    "✘ Error: failed to call get() function!".to_string()
                                )),
                            }
                        },
                        None => Err(PbtcCoreError::Custom(
                            "✘ Error: CALLBACK_GLOBAL_REF is None!".to_string()
                        )),
                    }  
                },
                None => Err(PbtcCoreError::Custom(
                    "✘ Error: MAYBE_JNI_JAVAVM is None!".to_string()
                )),
            }
        }
    }
}

pub fn get_database() -> Result<Database> {
    Database::open()
}
