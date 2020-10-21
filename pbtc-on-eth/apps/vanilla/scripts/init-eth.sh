#!/bin/bash
./pbtc_app initializeEth \
"./ptoken-erc777-bytecode" \
--confs=0 \
--chainId=3 \
--gasPrice=20000000000 \
--file=./eth-block.json
