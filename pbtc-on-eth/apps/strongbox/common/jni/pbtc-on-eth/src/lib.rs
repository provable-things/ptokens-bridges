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

use ptokens_core::chains::eth::eth_message_signer::sign_message_with_eth_key;

use ptokens_core::btc_on_eth::{
    debug_mint_pbtc,
    get_enclave_state,
    debug_get_all_utxos,
    debug_get_all_db_keys,
    debug_get_key_from_db,
    debug_clear_all_utxos,
    get_latest_block_numbers,
    debug_reprocess_btc_block,
    debug_reprocess_eth_block,
    debug_maybe_add_utxo_to_db,
    submit_btc_block_to_enclave,
    submit_eth_block_to_enclave,
    debug_set_key_in_db_to_value,
    maybe_initialize_btc_enclave,
    maybe_initialize_eth_enclave,
    debug_get_signed_erc777_change_pnetwork_tx,
    debug_get_signed_erc777_proxy_change_pnetwork_tx,
    debug_get_signed_erc777_proxy_change_pnetwork_by_proxy_tx,
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
pub extern "C" fn Java_io_ptokens_pbtconeth_Main_getEnclaveState(
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
pub extern "C" fn Java_io_ptokens_pbtconeth_Main_submitEthBlock(
    _env: JNIEnv, 
    _class: JClass, 
    _callback: JObject,
    _block: JString
) -> jstring {

    unsafe { set_callback_global_ref(&_env, &_callback); }
    
    let eth_block: String = _env.get_string(_block)
        .expect("✘ Couldn't convert java.lang.String to String")
        .into();

    info!("ETH Block: {:?}", eth_block);  

    let db = get_database()
        .expect("✘ Failed to open the database");

    let result = match submit_eth_block_to_enclave(db, &eth_block) {
        Ok(r) => r,
        Err(e) => format!("{{\"error\": {:?}}}", e.to_string())
    }; 

    _env.new_string(&result)
        .unwrap()
        .into_inner()
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "C" fn Java_io_ptokens_pbtconeth_Main_submitBtcBlock(
    _env: JNIEnv, 
    _class: JClass, 
    _callback: JObject,
    _block: JString
) -> jstring {    

    unsafe { set_callback_global_ref(&_env, &_callback); }
    
    let btc_block: String = _env.get_string(_block)
        .expect("✘ Couldn't convert java.lang.String to String")
        .into();

    let db = get_database()
        .expect("✘ Failed to open the database");

    info!("BTC Block: {:?}", btc_block);

    let result = match submit_btc_block_to_enclave(db, &btc_block) {
        Ok(r) => r,
        Err(e) => format!("{{\"error\": {:?}}}", e.to_string())
    }; 

    _env.new_string(&result)
        .unwrap()
        .into_inner()
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "C" fn Java_io_ptokens_pbtconeth_Main_initializeEth(
    _env: JNIEnv, 
    _class: JClass, 
    _callback: JObject, 
    _block: JString,
    _chain_id: u8,
    _gas_price: u64,
    _canon_to_tip_length: u64
) -> jstring {
    
    unsafe { set_callback_global_ref(&_env, &_callback); }

    info!("initializeEth: _chain_id: {:?} _gas_price: {:?} _canon_to_tip_length: {:?}", 
        _chain_id, 
        _gas_price,
        _canon_to_tip_length
    );

    let json_block: String = _env.get_string(_block)
          .expect("✘ Couldn't convert java.lang.String to String")
          .into();

    info!("ETH Block: {:?}", json_block);

    let db = get_database()
        .expect("✘ Failed to open the database");

    let _bytecode_path = "/data/local/tmp/smart-contract-bytecode".to_string();

    let result = match maybe_initialize_eth_enclave(
        db, 
        &json_block, 
        _chain_id, 
        _gas_price, 
        _canon_to_tip_length,
        &_bytecode_path
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
pub extern "C" fn Java_io_ptokens_pbtconeth_Main_initializeBtc(
    _env: JNIEnv, 
    _class: JClass, 
    _callback: JObject,
    _block: JString,
    _fee: u64,
    _difficulty: u64,
    _network: JString,
    _canon_to_tip_length: u64
) -> jstring {    
    unsafe { set_callback_global_ref(&_env, &_callback); }

    let block_json: String = _env.get_string(_block)
        .expect("✘ Couldn't convert java.lang.String to String")
        .into();

    let network: String = _env.get_string(_network)
        .expect("✘ Couldn't convert java.lang.String to String")
        .into();

    info!("initializeBtc: _fee: {:?} _difficulty: {:?} _network: {:?} _canon_to_tip_length {:?}", 
        _fee,
        _difficulty,
        network,
        _canon_to_tip_length
    );

    info!("BTC Block: {:?}", block_json);

    let db = get_database()
        .expect("✘ Failed to open the database");

    let result = match maybe_initialize_btc_enclave(
        db, 
        &block_json, 
        _fee, 
        _difficulty, 
        &network, 
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
pub extern "C" fn Java_io_ptokens_pbtconeth_Main_getLatestBlockNumbers(
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
pub extern "C" fn Java_io_ptokens_pbtconeth_Main_signMessageWithEthKey(
    _env: JNIEnv, 
    _class: JClass, 
    _callback: JObject,
    _message: JString
) -> jstring {    
    unsafe { set_callback_global_ref(&_env, &_callback); }

    let message: String = _env
        .get_string(_message)
        .expect("✘ Couldn't convert java.lang.String to String")
        .into();

    info!("signMessageWithKey: {:?}", message);

    let _db = get_database()
        .expect("✘ Failed to open the database");

    let result = sign_message_with_eth_key(&_db, message)
        .unwrap()
        .to_string();

    _env.new_string(&result)
        .unwrap()
        .into_inner()
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "C" fn Java_io_ptokens_pbtconeth_Main_debugGetAllUtxos(
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
pub extern "C" fn Java_io_ptokens_pbtconeth_Main_debugSetKeyInDbToValue(
    _env: JNIEnv, 
    _class: JClass, 
    _callback: JObject,
    _key: JString,
    _value: JString
) -> jstring {
    unsafe { set_callback_global_ref(&_env, &_callback); }

    let key: String = _env
          .get_string(_key)
          .expect("✘ Couldn't convert the key from java to rust!")
          .into();

    let value: String = _env
          .get_string(_value)
          .expect("✘ Couldn't convert the value from java to rust!")
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
pub extern "C" fn Java_io_ptokens_pbtconeth_Main_debugGetKeyFromDb(
    _env: JNIEnv, 
    _class: JClass, 
    _callback: JObject,
    _key: JString
) -> jstring {

    unsafe { set_callback_global_ref(&_env, &_callback); }

    let key: String = _env
          .get_string(_key)
          .expect("✘ Couldn't convert the key from java to rust!")
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
pub extern "C" fn Java_io_ptokens_pbtconeth_Main_debugReprocessEthBlock(
    _env: JNIEnv,
    _class: JClass,
    _callback: JObject,
    _block: JString
) -> jstring {
    unsafe { set_callback_global_ref(&_env, &_callback); }

    let block_json: String = _env.get_string(_block)
        .expect("✘ Couldn't convert java.lang.String to String")
        .into();

    let db = get_database()
        .expect("✘ Failed to open the database");

    let result = match debug_reprocess_eth_block(db, &block_json) {
        Ok(r) => r,
        Err(e) => format!("{{\"error\": {:?}}}", e.to_string())
    }; 

    _env.new_string(&result)
        .unwrap()
        .into_inner()
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "C" fn Java_io_ptokens_pbtconeth_Main_debugReprocessBtcBlock(
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

    let result = match debug_reprocess_btc_block(db, &block_json) {
        Ok(r) => r,
        Err(e) => format!("{{\"error\": {:?}}}", e.to_string())
    }; 

    _env.new_string(&result)
        .unwrap()
        .into_inner()
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "C" fn Java_io_ptokens_pbtconeth_Main_debugGetAllDbKeys(
    _env: JNIEnv, 
    _class: JClass, 
    _callback: JObject
) -> jstring {
    unsafe { set_callback_global_ref(&_env, &_callback); }

    let result = debug_get_all_db_keys()
        .unwrap();

    _env.new_string(&result)
        .unwrap()
        .into_inner()
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "C" fn Java_io_ptokens_pbtconeth_Main_debugErc777ChangePNetwork(
    _env: JNIEnv, 
    _class: JClass, 
    _callback: JObject,
    _new_address: JString
) -> jstring {    
    unsafe { set_callback_global_ref(&_env, &_callback); }

    let new_address: String = _env
        .get_string(_new_address)
        .expect("✘ Couldn't convert java.lang.String to String")
        .into();

    let db = get_database()
        .expect("✘ Failed to open the database");

    let result = match debug_get_signed_erc777_change_pnetwork_tx(db, &new_address) {
        Ok(r) => r,
        Err(e) => format!("{{\"error\": {:?}}}", e.to_string())
    }; 

    _env.new_string(&result)
        .unwrap()
        .into_inner()
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "C" fn Java_io_ptokens_pbtconeth_Main_debugErc777ProxyChangePNetwork(
    _env: JNIEnv, 
    _class: JClass, 
    _callback: JObject,
    _new_address: JString
) -> jstring {    
    unsafe { set_callback_global_ref(&_env, &_callback); }

    let new_address: String = _env
        .get_string(_new_address)
        .expect("✘ Couldn't convert java.lang.String to String")
        .into();

    let db = get_database()
        .expect("✘ Failed to open the database");

    let result = match debug_get_signed_erc777_proxy_change_pnetwork_tx(db, &new_address) {
        Ok(r) => r,
        Err(e) => format!("{{\"error\": {:?}}}", e.to_string())
    }; 
    
    _env.new_string(&result)
        .unwrap()
        .into_inner()
}


#[no_mangle]
#[allow(non_snake_case)]
pub extern "C" fn Java_io_ptokens_pbtconeth_Main_debugErc777ProxyChangePNetworkByProxy(
    _env: JNIEnv, 
    _class: JClass, 
    _callback: JObject,
    _new_address: JString
) -> jstring {
    unsafe { set_callback_global_ref(&_env, &_callback); }

    let new_address: String = _env
        .get_string(_new_address)
        .expect("✘ Couldn't convert java.lang.String to String")
        .into();

    let db = get_database()
        .expect("✘ Failed to open the database");

    let result = match debug_get_signed_erc777_proxy_change_pnetwork_by_proxy_tx(db, &new_address) {
        Ok(r) => r,
        Err(e) => format!("{{\"error\": {:?}}}", e.to_string())
    }; 
    
    _env.new_string(&result)
        .unwrap()
        .into_inner()
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "C" fn Java_io_ptokens_pbtconeth_Main_debugClearAllUtxos(
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
pub extern "C" fn Java_io_ptokens_pbtconeth_Main_debugAddUtxoToDb(
    _env: JNIEnv, 
    _class: JClass, 
    _callback: JObject,
    _submissionMaterial: JString
) -> jstring {
    unsafe { set_callback_global_ref(&_env, &_callback); }

    let submissionmaterial: String = _env
        .get_string(_submissionMaterial)
        .expect("✘ Couldn't convert java.lang.String to String")
        .into();

    let db = get_database()
        .expect("✘ Failed to open the database");

    let result = match debug_maybe_add_utxo_to_db(db, &submissionmaterial) {
        Ok(r) => r,
        Err(e) => format!("{{\"error\": {:?}}}", e.to_string())
    }; 

    _env.new_string(&result)
        .unwrap()
        .into_inner()
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "C" fn Java_io_ptokens_pbtconeth_Main_debugMintTransaction(
    _env: JNIEnv, 
    _class: JClass, 
    _callback: JObject, 
    _amount: u128,
    _nonce: u64,
    _eth_network: JString,
    _gas_price: u64,
    _recipient: JString
) -> jstring {
    unsafe { set_callback_global_ref(&_env, &_callback); }

    let eth_network: String = _env
          .get_string(_eth_network)
          .expect("✘ Couldn't convert java.lang.String to String")
          .into();

    let recipient: String = _env
          .get_string(_recipient)
          .expect("✘ Couldn't convert java.lang.String to String")
          .into();

    info!("debugMintTransaction: _amount {:?} _nonce {:?} _eth_network {:?} _gas_price {:?} _recipient {:?}",
        _amount,
        _nonce,
        eth_network,
        _gas_price,
        recipient
    );

    let db = get_database()
        .expect("✘ Failed to open the database");

    let result = match debug_mint_pbtc(
        db, 
        _amount, 
        _nonce, 
        &eth_network, 
        _gas_price,
        &recipient
    ) {
        Ok(r) => r,
        Err(e) => format!("{{\"error\": {:?}}}", e.to_string())
    };

    _env.new_string(&result)
        .unwrap()
        .into_inner()
}