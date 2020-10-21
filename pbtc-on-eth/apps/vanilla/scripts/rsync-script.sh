#!/bin/bash
rsync -rvz --progress --verbose \
--rsh='ssh -p 2222' \
../../enclave/ \
$nucIP:/home/greg/enclave/ \
--exclude "/logs" \
--exclude "/target" \
--exclude "app/target" \
--exclude "enclave/target" \
--exclude='.*.swp'
