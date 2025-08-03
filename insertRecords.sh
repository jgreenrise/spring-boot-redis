#!/bin/bash

# Base URL
BASE_URL="http://localhost:8080/sortedset/add/mySortedSet?key=leaderboardOct2023"

# Loop through 500 users
for ((i=1; i<=500; i++)); do
  USER_ID="user$i"
  SCORE=1

  # Construct the cURL command
  CURL_COMMAND="curl --location --request POST '$BASE_URL&user=$USER_ID&score=$SCORE'"

  # Execute the cURL command
  echo "Adding user: $USER_ID with score: $SCORE"
  eval $CURL_COMMAND

  # Delay between requests (adjust as needed)
  sleep 1
done

