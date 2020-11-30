pub mod types;
pub mod errors;
pub mod constants;
pub mod get_database;
pub mod initialize_logger;

extern crate ptokens_core;
extern crate simplelog;
#[macro_use] extern crate log;

use crate::{
    get_database::get_database,
    initialize_logger::initialize_logger,
};


use ptokens_core::btc_on_eos::{
    get_enclave_state,
    debug_get_all_utxos,
    debug_get_key_from_db,
    debug_get_all_db_keys,
    get_latest_block_numbers,
    debug_update_incremerkle,
    submit_btc_block_to_core,
    submit_eos_block_to_core,
    maybe_initialize_btc_core,
    maybe_initialize_eos_core,
    debug_add_new_eos_schedule,
    enable_eos_protocol_feature,
    disable_eos_protocol_feature,
    debug_set_key_in_db_to_value,
    debug_get_child_pays_for_parent_btc_tx,
    debug_reprocess_btc_block_for_stale_eos_tx,
    debug_clear_all_utxos,
    debug_reprocess_eos_block
};


use jni::{
  sys::{
    jint,
    jstring,
  },
  objects::{
    JClass, 
    JObject, 
    JValue, 
    JString,
    GlobalRef
  },
  JavaVM,
  JNIEnv
};


static mut MAYBE_JNI_JAVAVM: Option<JavaVM> = None;
static mut CALLBACK_GLOBAL_REF: Option<GlobalRef> = None;
pub type Bytes = Vec<u8>;

unsafe fn set_callback_global_ref(
    _env: &JNIEnv, 
    _callback: &JObject
) {
    CALLBACK_GLOBAL_REF = Some(_env.new_global_ref(*_callback)
        .expect("✘ Failed to store global reference")
    );
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn JNI_OnLoad(_vm: JavaVM) -> jint { 

    match initialize_logger() {
        Ok(_) => info!("✔ Logs initialized!"),
        Err(e) => info!("✘ Logs not initialized {}", e)
    };

    unsafe {
      match &MAYBE_JNI_JAVAVM {
          Some(_) => info!("JavaVM object already set"),
          None => {
            MAYBE_JNI_JAVAVM = Some(_vm);
            info!("Native library loaded");
          },
      }
    }
    jni::sys::JNI_VERSION_1_6
}


#[no_mangle]
#[allow(non_snake_case)]
pub extern "C" fn Java_io_ptokens_pbtconeos_Main_submitEosBlock(
    _env: JNIEnv, 
    _class: JClass, 
    _callback: JObject,
    _payload: JString
) -> jstring {

    unsafe { set_callback_global_ref(&_env, &_callback); }
    
    let blockJson: String = _env
        .get_string(_payload)
        .expect("✘ Couldn't convert java.lang.String to String")
        .into();

    info!("EOS Block: {:?}", blockJson);

    let db = get_database()
        .expect("✘ Failed to open the database");

    let result = match submit_eos_block_to_core(db, &blockJson) {
        Ok(r) => r,
        Err(e) => format!("{{\"error\": {:?}}}", e.to_string())
    }; 

    _env.new_string(&result)
        .unwrap()
        .into_inner()
}


#[no_mangle]
#[allow(non_snake_case)]
pub extern "C" fn Java_io_ptokens_pbtconeos_Main_submitBtcBlock(
    _env: JNIEnv, 
    _class: JClass, 
    _callback: JObject,
    _payload: JString
) -> jstring {    
    
    unsafe { set_callback_global_ref(&_env, &_callback); }
    
    let blockJson: String = _env
        .get_string(_payload)
        .expect("✘ Couldn't convert java.lang.String to String")
        .into();

    info!("BTC Block: {:?}", blockJson);

    let db = get_database()
        .expect("✘ Failed to open the database");

    let result = match submit_btc_block_to_core(db, &blockJson) {
        Ok(r) => r,
        Err(e) => format!("{{\"error\": {:?}}}", e.to_string())
    }; 

    _env.new_string(&result)
        .unwrap()
        .into_inner()
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "C" fn Java_io_ptokens_pbtconeos_Main_debugReprocessBtcBlock(
    _env: JNIEnv, 
    _class: JClass, 
    _callback: JObject,
    _block: JString
  ) -> jstring {    
    unsafe { set_callback_global_ref(&_env, &_callback); }
    
    info!("lib::debugReprocessBtcBlock called");
      
    let block_json: String = _env.get_string(_block)
        .expect("✘ Couldn't convert java.lang.String to String")
        .into();

    let db = get_database()
        .expect("✘ Failed to open the database");

    let result = match debug_reprocess_btc_block_for_stale_eos_tx(db, &block_json) {
        Ok(r) => r,
        Err(e) => format!("{{\"error\": {:?}}}", e.to_string())
    }; 

    _env.new_string(&result)
        .unwrap()
        .into_inner()
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "C" fn Java_io_ptokens_pbtconeos_Main_initializeEos(
    _env: JNIEnv, 
    _class: JClass, 
    _callback: JObject, 
    _chain_id: JString,
    _account_name: JString,
    _token_symbol: JString,
    _eos_init_json: JString
  ) -> jstring {
    unsafe { set_callback_global_ref(&_env, &_callback); }

    let account_name: String = _env
          .get_string(_account_name)
          .expect("✘ Couldn't convert java.lang.String to String")
          .into();

    let chain_id: String = _env
          .get_string(_chain_id)
          .expect("✘ Couldn't convert java.lang.String to String")
          .into();
    
    let token_symbol: String = _env
          .get_string(_token_symbol)
          .expect("✘ Couldn't convert java.lang.String to String")
          .into();

    let eos_init_json: String = _env
          .get_string(_eos_init_json)
          .expect("✘ Couldn't convert java.lang.String to String")
          .into();

    info!("✔ Initialize EOS: chain_id: {:?} account_name: {:?} symbol: {:?}", 
        chain_id, 
        account_name,
        token_symbol
    );

    info!("✔ EOS Block {:?} ", eos_init_json);

    let db = get_database()
        .expect("✘ Failed to open the database");
    
    let result = match maybe_initialize_eos_core(
        db, 
        &chain_id, 
        &account_name,
        &token_symbol,
        &eos_init_json
    ) {
        Ok(r) => r,
        Err(e) => format!("{{\"error\": {:?}}}", e.to_string())
    };

    _env.new_string(&result)
        .unwrap()
        .into_inner()
}


#[no_mangle]
#[allow(non_snake_case)]
pub extern "C" fn Java_io_ptokens_pbtconeos_Main_initializeBtc(
    _env: JNIEnv, 
    _class: JClass, 
    _callback: JObject,
    _payload: JString,
    _fee: u64,
    _difficulty: u64,
    _network: JString,
    _canon_to_tip_length: u64
) -> jstring {

    unsafe { set_callback_global_ref(&_env, &_callback); }

    let block_json: String = _env
          .get_string(_payload)
          .expect("✘ Couldn't convert java.lang.String to String")
          .into();

    let network: String = _env
          .get_string(_network)
          .expect("✘ Couldn't convert java.lang.String to String")
          .into();

    info!("Params: _fee: {:?} _difficulty: {:?} _network: {:?} _canon_to_tip_length {:?}", 
        _fee,
        _difficulty,
        network,
        _canon_to_tip_length
    );

    info!("BTC Block: {:?}", block_json);

    let db = get_database()
        .expect("✘ Failed to open the database");

    let result = match maybe_initialize_btc_core(
        db, 
        &block_json, 
        _fee, 
        _difficulty, 
        &network.to_string(), 
        _canon_to_tip_length
    ) {
        Ok(r) => r,
        Err(e) => format!("{{\"error\": {:?}}}", e.to_string())
    };

    _env.new_string(&result)
        .unwrap()
        .into_inner()
}


#[no_mangle]
#[allow(non_snake_case)]
pub extern "C" fn Java_io_ptokens_pbtconeos_Main_getLatestBlockNumbers(
    _env: JNIEnv, 
    _class: JClass, 
    _callback: JObject
) -> jstring {

    unsafe { set_callback_global_ref(&_env, &_callback); }

    let db = get_database()
        .expect("✘ Failed to open the database");

    let result = match get_latest_block_numbers(db) {
        Ok(r) => r,
        Err(e) => format!("{{\"error\": {:?}}}", e.to_string())
    }; 

    _env.new_string(&result)
        .unwrap()
        .into_inner()
}


#[no_mangle]
#[allow(non_snake_case)]
pub extern "C" fn Java_io_ptokens_pbtconeos_Main_getEnclaveState(
    _env: JNIEnv, 
    _class: JClass, 
    _callback: JObject
) -> jstring {
    
    unsafe { set_callback_global_ref(&_env, &_callback); }
    
    let db = get_database()
        .expect("✘ Failed to open the database");

    let result = get_enclave_state(db)
        .expect("✘ Failed to get the state");

    _env.new_string(&result)
        .unwrap()
        .into_inner()
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "C" fn Java_io_ptokens_pbtconeos_Main_debugGetAllUtxos(
    _env: JNIEnv, 
    _class: JClass, 
    _callback: JObject
) -> jstring {

    unsafe { set_callback_global_ref(&_env, &_callback); }
    
    let db = get_database()
        .expect("✘ Failed to open the database");

    let result = match debug_get_all_utxos(db) {
        Ok(r) => r,
        Err(e) => format!("{{\"error\": {:?}}}", e.to_string())
    }; 

    _env.new_string(&result)
        .unwrap()
        .into_inner()
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "C" fn Java_io_ptokens_pbtconeos_Main_debugSetKeyInDbToValue(
    _env: JNIEnv, 
    _class: JClass, 
    _callback: JObject,
    _key: JString,
    _value: JString
) -> jstring {

    unsafe { set_callback_global_ref(&_env, &_callback); }

    let key: String = _env
          .get_string(_key)
          .expect("Couldn't convert the key from java to rust!")
          .into();

    let value: String = _env
          .get_string(_value)
          .expect("Couldn't convert the value from java to rust!")
          .into();

    let db = get_database()
        .expect("✘ Failed to open the database");

    let result = match debug_set_key_in_db_to_value(db, &key, &value) {
        Ok(r) => r,
        Err(e) => format!("{{\"error\": {:?}}}", e.to_string())
    }; 

    _env.new_string(&result)
        .unwrap()
        .into_inner()   
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "C" fn Java_io_ptokens_pbtconeos_Main_debugGetKeyFromDb(
    _env: JNIEnv, 
    _class: JClass, 
    _callback: JObject,
    _key: JString
) -> jstring {

    unsafe { set_callback_global_ref(&_env, &_callback); }

    let key: String = _env
          .get_string(_key)
          .expect("Couldn't convert the key from java to rust!")
          .into();

    let db = get_database()
        .expect("✘ Failed to open the database");

    let result = match debug_get_key_from_db(db, &key) {
        Ok(r) => r,
        Err(e) => format!("{{\"error\": {:?}}}", e.to_string())
    }; 

    _env.new_string(&result)
        .unwrap()
        .into_inner()
}


#[no_mangle]
#[allow(non_snake_case)]
pub extern "C" fn Java_io_ptokens_pbtconeos_Main_debugAddNewEosSchedule(
    _env: JNIEnv, 
    _class: JClass, 
    _callback: JObject,
    _schedule_json: JString
) -> jstring {    
    unsafe { set_callback_global_ref(&_env, &_callback); }

    let schedule_json: String = _env
          .get_string(_schedule_json)
          .expect("✘ Couldn't convert java.lang.String to String")
          .into();

    info!("debugAddNewEosSchedule: _schedule_json: {:?}", schedule_json);

    let db = get_database()
        .expect("✘ Failed to open the database");

    let result = match debug_add_new_eos_schedule(
        db, 
        &schedule_json
    ) {
        Ok(r) => r,
        Err(e) => format!("{{\"error\": {:?}}}", e.to_string())
    }; 

    _env.new_string(&result)
        .unwrap()
        .into_inner()
}


#[no_mangle]
#[allow(non_snake_case)]
pub extern "C" fn Java_io_ptokens_pbtconeos_Main_debugUpdateIncremerkle(
    _env: JNIEnv, 
    _class: JClass, 
    _callback: JObject,
    _eos_json: JString
) -> jstring {    
    unsafe { set_callback_global_ref(&_env, &_callback); }

    let eos_json: String = _env
          .get_string(_eos_json)
          .expect("✘ Couldn't convert java.lang.String to String")
          .into();

    info!("debugUpdateIncremerkle: _eos_json: {:?}", eos_json);

    let db = get_database()
        .expect("✘ Failed to open the database");

    let result = match debug_update_incremerkle(
        &db, 
        &eos_json
    ) {
        Ok(r) => r,
        Err(e) => format!("{{\"error\": {:?}}}", e.to_string())
    }; 

    _env.new_string(&result)
        .unwrap()
        .into_inner()
}


#[no_mangle]
#[allow(non_snake_case)]
pub extern "C" fn Java_io_ptokens_pbtconeos_Main_enableEosProtocolFeature(
    _env: JNIEnv, 
    _class: JClass, 
    _callback: JObject,
    _feature_hash: JString
) -> jstring  {    
    unsafe { set_callback_global_ref(&_env, &_callback); }

    let feature_hash: String = _env
          .get_string(_feature_hash)
          .expect("✘ Couldn't convert java.lang.String to String")
          .into();

    info!("enableEosProtocolFeature: _feature_hash: {:?}", feature_hash);

    let db = get_database()
        .expect("✘ Failed to open the database");

    let result = match enable_eos_protocol_feature(
        db, 
        &feature_hash
    ) {
        Ok(r) => r,
        Err(e) => format!("{{\"error\": {:?}}}", e.to_string())
    }; 

    _env.new_string(&result)
        .unwrap()
        .into_inner()
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "C" fn Java_io_ptokens_pbtconeos_Main_disableEosProtocolFeature(
    _env: JNIEnv,   
    _class: JClass, 
    _callback: JObject,
    _feature_hash: JString
) -> jstring {    
    unsafe { set_callback_global_ref(&_env, &_callback); }

    let feature_hash: String = _env
          .get_string(_feature_hash)
          .expect("✘ Couldn't convert java.lang.String to String")
          .into();

    info!("disableEosProtocolFeature: _feature_hash: {:?}", feature_hash);

    let db = get_database()
        .expect("✘ Failed to open the database");

    let result = match disable_eos_protocol_feature(
        db, 
        &feature_hash
    ) {
        Ok(r) => r,
        Err(e) => format!("{{\"error\": {:?}}}", e.to_string())
    }; 

    _env.new_string(&result)
        .unwrap()
        .into_inner()
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "C" fn Java_io_ptokens_pbtconeos_Main_debugGetAllDbKeys(
    _env: JNIEnv, 
    _class: JClass, 
    _callback: JObject
) -> jstring {    
    unsafe { set_callback_global_ref(&_env, &_callback); }


    let result = match debug_get_all_db_keys() {
        Ok(r) => r,
        Err(e) => format!("{{\"error\": {:?}}}", e.to_string())
    }; 

    _env.new_string(&result)
        .unwrap()
        .into_inner()
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "C" fn Java_io_ptokens_pbtconeos_Main_debugClearAllUtxos(
     _env: JNIEnv, 
    _class: JClass, 
    _callback: JObject
) -> jstring {    
    unsafe { set_callback_global_ref(&_env, &_callback); }

    let db = get_database()
        .expect("✘ Failed to open the database");

    let result = match debug_clear_all_utxos(&db) {
        Ok(r) => r,
        Err(e) => format!("{{\"error\": {:?}}}", e.to_string())
    }; 

    _env.new_string(&result)
        .unwrap()
        .into_inner()
}


#[no_mangle]
#[allow(non_snake_case)]
pub extern "C" fn Java_io_ptokens_pbtconeos_Main_debugReprocessEosBlock(
    _env: JNIEnv,
    _class: JClass,
    _callback: JObject,
    _payload: JString
) -> jstring {
    unsafe { set_callback_global_ref(&_env, &_callback); }

    let block_json: String = _env
        .get_string(_payload)
        .expect("✘ Couldn't convert java.lang.String to String")
        .into();

    let db = get_database()
        .expect("✘ Failed to open the database");

    let result = match debug_reprocess_eos_block(db, &block_json) {
        Ok(r) => r,
        Err(e) => format!("{{\"error\": {:?}}}", e.to_string())
    }; 

    _env.new_string(&result)
        .unwrap()
        .into_inner()
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "C" fn Java_io_ptokens_pbtconeos_Main_debugGetChildPaysForParentTx(
    _env: JNIEnv,
    _class: JClass,
    _callback: JObject,
    _fee: u64,
    _txId: JString,
    _vOut: u32
) -> jstring {
    unsafe { set_callback_global_ref(&_env, &_callback); }

    let txId: String = _env
        .get_string(_txId)
        .expect("✘ Couldn't convert java.lang.String to String")
        .into();

    let db = get_database()
        .expect("✘ Failed to open the database");

    let result = match debug_get_child_pays_for_parent_btc_tx(db, _fee, &txId, _vOut) {
        Ok(r) => r,
        Err(e) => format!("{{\"error\": {:?}}}", e.to_string())
    }; 

    _env.new_string(&result)
        .unwrap()
        .into_inner()
}