pub mod types;
pub mod errors;
pub mod constants;
pub mod usage_info;
pub mod get_cli_args;
pub mod get_database;
pub mod initialize_logger;
pub mod initialize_database;

extern crate docopt;
extern crate rocksdb;
extern crate simplelog;
extern crate ptokens_core;
#[macro_use] extern crate log;
#[macro_use] extern crate serde_derive;

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
    debug_reprocess_btc_block_for_stale_eos_tx,
};

use crate::{
    errors::AppError,
    usage_info::USAGE_INFO,
    get_database::get_database,
    initialize_logger::initialize_logger,
    initialize_database::maybe_initialize_database,
    get_cli_args::{
        CliArgs,
        get_cli_args,
    },
};

fn main() -> Result<(), AppError> {
    match initialize_logger()
        .and_then(|_| maybe_initialize_database())
        .and_then(|_| get_cli_args())
        .and_then(|cli_args|
            match cli_args {
                CliArgs {cmd_initializeEos: true, ..} => {
                    info!("✔ Maybe initializing EOS core...");
                    Ok(
                        maybe_initialize_eos_core(
                            get_database()?,
                            &cli_args.flag_chainId,
                            &cli_args.flag_accountName,
                            &cli_args.flag_symbol,
                            &cli_args.arg_eosJson,
                        )?
                    )
                }
                CliArgs {cmd_initializeBtc: true, ..} => {
                    info!("✔ Initializing BTC core...");
                    Ok(
                        maybe_initialize_btc_core(
                            get_database()?,
                            &cli_args.arg_blockJson,
                            cli_args.flag_fee,
                            cli_args.flag_difficulty,
                            &cli_args.flag_network,
                            cli_args.flag_confs,
                        )?
                    )
                }
                CliArgs {cmd_debugGetAllDbKeys: true, ..} => {
                    info!("✔ Debug getting all DB keys...");
                    Ok(debug_get_all_db_keys()?)
                }
                CliArgs {cmd_getEnclaveState: true, ..} => {
                    info!("✔ Getting core state...");
                    Ok(get_enclave_state(get_database()?)?)
                }
                CliArgs {cmd_getLatestBlockNumbers: true, ..} => {
                    info!("✔ Maybe getting block numbers...");
                    Ok(get_latest_block_numbers(get_database()?)?)
                }
                CliArgs {cmd_debugGetAllUtxos: true, ..} => {
                    info!("✔ Getting all UTXOs from the database...");
                    Ok(debug_get_all_utxos(get_database()?)?)
                }
                CliArgs {cmd_debugGetKeyFromDb: true, ..} => {
                    info!("✔ Maybe getting a key from the database...");
                    Ok(debug_get_key_from_db(get_database()?, &cli_args.arg_key)?)
                }
                CliArgs {cmd_debugUpdateIncremerkle: true, ..} => {
                    info!("✔ Debug updating EOS incremerkle...");
                    Ok(debug_update_incremerkle(&get_database()?, &cli_args.arg_eosJson)?)
                }
                CliArgs {cmd_submitEosBlock: true, ..} => {
                    info!("✔ Submitting EOS block to core...");
                    Ok(submit_eos_block_to_core(get_database()?, &cli_args.arg_blockJson)?)
                }
                CliArgs {cmd_submitBtcBlock: true, ..} => {
                    info!("✔ Submitting BTC block to core...");
                    Ok(submit_btc_block_to_core(get_database()?, &cli_args.arg_blockJson)?)
                }
                CliArgs {cmd_enableEosProtocolFeature: true, ..} => {
                    info!("✔ Enabled EOS protocol feature...");
                    Ok(enable_eos_protocol_feature(get_database()?, &cli_args.arg_featureHash)?)
                }
                CliArgs {cmd_disableEosProtocolFeature: true, ..} => {
                    info!("✔ Disabling EOS protocol feature...");
                    Ok(disable_eos_protocol_feature(get_database()?, &cli_args.arg_featureHash)?)
                }
                CliArgs {cmd_debugAddEosSchedule: true, ..} => {
                    info!("✔ Adding EOS schedule to database...");
                    Ok(debug_add_new_eos_schedule(get_database()?, &cli_args.arg_scheduleJson)?)
                }
                CliArgs {cmd_debugReprocessBtcBlock: true, ..} => {
                    info!("✔ Debug reprocessing BTC block...");
                    Ok(debug_reprocess_btc_block_for_stale_eos_tx(get_database()?, &cli_args.arg_blockJson)?)
                }
                CliArgs {cmd_debugSetKeyInDbToValue: true, ..} => {
                    info!("✔ Setting a key in the database to a value...");
                    Ok(debug_set_key_in_db_to_value(get_database()?, &cli_args.arg_key, &cli_args.arg_value)?)
                }
                _ => Err(AppError::Custom(USAGE_INFO.to_string()))
            }
        ) {
            Ok(json_string) => {
                trace!("{}", json_string);
                println!("{}", json_string);
                Ok(())
            },
            Err(e) => {
                error!("{}", e);
                println!("{}", e);
                std::process::exit(1);
            }
        }
}
