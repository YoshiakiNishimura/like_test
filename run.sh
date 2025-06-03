#!/bin/bash
# abcdefghijklmnop
# if tsurugi not start
# tgctl start
# if first run
# sbt run
echo "test begin"
echo "default"
time tgsql --exec -c ipc:tsurugi -t RTX --with PARALLEL=1 "select count(id) from like_table" ;
echo "a%"
time tgsql --exec -c ipc:tsurugi -t RTX --with PARALLEL=1 "select count(id) from like_table WHERE name LIKE 'a%' " ;
echo "%p"
time tgsql --exec -c ipc:tsurugi -t RTX --with PARALLEL=1 "select count(id) from like_table WHERE name LIKE '%p' " ;
echo "%h%"
time tgsql --exec -c ipc:tsurugi -t RTX --with PARALLEL=1 "select count(id) from like_table WHERE name LIKE '%h%' " ;
echo "%a%b%c%d%e%f%g%h%i%j%k%l%m%n%o%p%"
time tgsql --exec -c ipc:tsurugi -t RTX --with PARALLEL=1 "select count(id) from like_table WHERE name LIKE '%a%b%c%d%e%f%g%h%i%j%k%l%m%n%o%p%' " ;


