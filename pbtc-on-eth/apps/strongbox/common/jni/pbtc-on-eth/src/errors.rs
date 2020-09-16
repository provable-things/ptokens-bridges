use std::fmt;
use std::error::Error;

#[derive(Debug)]
pub enum AppError {
    Custom(String),
    IOError(std::io::Error),
    PbtcAppError(ptokens_core::errors::AppError),
    SystemTimeError(std::time::SystemTimeError),
}

impl fmt::Display for AppError {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        let msg = match *self {
            AppError::Custom(ref msg) =>
                format!("{}", msg),
            AppError::IOError(ref e) =>
                format!("✘ I/O Error!\n✘ {}", e),
            AppError::PbtcAppError(ref e) =>
                format!("{}", e),
            AppError::SystemTimeError(ref e) =>
                format!("✘ System Time Error!\n✘ {}", e),
        };
        f.write_fmt(format_args!("{}", msg))
    }
}

impl Error for AppError {
    fn description(&self) -> &str {
        "\n✘ Program Error!\n"
    }
}

impl From<std::io::Error> for AppError {
    fn from(e: std::io::Error) -> AppError {
        AppError::IOError(e)
    }
}

impl From<std::time::SystemTimeError> for AppError {
    fn from(e: std::time::SystemTimeError) -> AppError {
        AppError::SystemTimeError(e)
    }
}

impl From<ptokens_core::errors::AppError> for AppError {
    fn from(e: ptokens_core::errors::AppError) -> AppError {
        AppError::PbtcAppError(e)
    }
}