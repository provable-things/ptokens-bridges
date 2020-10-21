use docopt::Docopt;
use std::{
    path::Path,
    fs::read_to_string,
};
use crate::{
    types::Result,
    errors::AppError,
    usage_info::USAGE_INFO,
};

#[allow(non_snake_case)]
#[derive(Clone, Debug, Deserialize, PartialEq)]
pub struct CliArgs {
    pub arg_key: String,
    pub arg_path: String,
    pub flag_fee: u64,
    pub arg_utxo: String,
    pub flag_file: String,
    pub arg_value: String,
    pub flag_confs: u64,
    pub flag_nonce: u64,
    pub arg_address: String,
    pub flag_network: String, // NOTE: BTC network!
    pub flag_chainId: u8, // NOTE: ETH network!
    pub arg_blockJson: String,
    pub flag_gasPrice: u64,
    pub flag_difficulty: u64,
    pub flag_ethNetwork: String,
    pub flag_recipient: String,
    pub cmd_initializeEth: bool,
    pub cmd_initializeBtc: bool,
    pub cmd_submitEthBlock: bool,
    pub cmd_submitBtcBlock: bool,
    pub cmd_getEnclaveState: bool,
    pub cmd_debugGetAllUtxos: bool,
    pub cmd_debugGetAllDbKeys: bool,
    pub cmd_debugGetKeyFromDb: bool,
    pub cmd_debugAddUtxoToDb: bool,
    pub cmd_debugClearAllUtxos: bool,
    pub cmd_getLatestBlockNumbers: bool,
    pub cmd_debugReprocessBtcBlock: bool,
    pub cmd_debugReprocessEthBlock: bool,
    pub cmd_debugSetKeyInDbToValue: bool,
    pub cmd_debugErc777ChangePNetwork: bool,
    pub cmd_debugErc777ProxyChangePNetwork: bool,
    pub cmd_debugErc777ProxyChangePNetworkByProxy: bool,
}

impl CliArgs {
    pub fn update_block_in_cli_args(
        mut self,
        block_json: String
    ) -> Result<Self> {
        self.arg_blockJson = block_json;
        Ok(self)
    }
}

pub fn parse_cli_args() -> Result<CliArgs> {
    match Docopt::new(USAGE_INFO)
        .and_then(|d| d.deserialize()) {
            Ok(cli_args) => Ok(cli_args),
            Err(e) => Err(AppError::Custom(e.to_string()))
        }
}

pub fn maybe_read_block_json_from_file(
    cli_args: CliArgs
) -> Result<CliArgs> {
    match Path::new(&cli_args.flag_file).exists() {
        true => {
            info!(
                "✔ File exists @ path: {},\n✔ Reading file...",
                cli_args.flag_file,
            );
            cli_args
                .clone()
                .update_block_in_cli_args(read_to_string(cli_args.flag_file)?)
        }
        false => {
            info!(
                "✔ No file exists @ path: {}\n✔ Not reading file...",
                cli_args.flag_file,
            );
            Ok(cli_args)
        }
    }
}

pub fn get_cli_args() -> Result<CliArgs> {
    parse_cli_args()
        .and_then(maybe_read_block_json_from_file)
}
