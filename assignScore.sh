#!/bin/bash

# Base URL
BASE_URL="http://localhost:8080/sortedset/increment/mySortedSet?key=leaderboardOct2023"

# Loop through users from user1 to user100
for ((i=1; i<=100; i++)); do
  USER_ID="user$i"
  RANDOM_SCORE=$((1 + RANDOM % 1000))

  # Construct the cURL command
  CURL_COMMAND="curl --location --request PUT '$BASE_URL&user=$USER_ID&score=$RANDOM_SCORE'"

  # Execute the cURL command
  echo "Assigning random score to $USER_ID: $RANDOM_SCORE"
  eval $CURL_COMMAND

  # Delay between requests (adjust as needed)
  sleep 1
done

