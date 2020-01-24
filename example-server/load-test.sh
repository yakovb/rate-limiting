#!/usr/bin/env bash

function usage() {
  echo "Assumptions: "
  echo "- the example server is running on localhost:8080"
  echo "- curl is installed"
  echo ""
  echo "Behaviour:"
  echo "- for 2 'users' making requests to the example server..."
  echo "- launch a configurable number of requests in parallel for each user (default 10)..."
  echo "- count the number of successful requests and echo the results."
  echo ""
  echo "The idea is that the number of successful requests per user matches what's permitted in a given request time window."
  echo ""
  echo "Usage:"
  echo "  load-test.sh 15"
  echo ""
}

REQ_COUNT=10

if [ $# -gt 1 ]; then
  usage
  exit
elif [ $# -eq 1 ] && [ $1 = '--help' ]; then
  usage
  exit
elif [ $# -eq 1 ]; then
  REQ_COUNT=$1
fi

ENDPOINT='localhost:8080'

function do_request() {
  result=$(curl --write-out %{http_code} --silent --output /dev/null -H "user-id: abc" $ENDPOINT)
  case $result in
  429)
    echo "request $1 rejected with 429: TOO MANY REQUESTS"
    ;;
  200)
    echo "request $1 success!!"
    ;;
  *)
    echo "request $1 unexpectedly failed with code $result"
  esac
}

for count in $(seq 1 $REQ_COUNT); do
  do_request $count &
done
