#!/bin/bash

echo "
              |    |    |
             )_)  )_)  )_)
            )___))___))___)\\
           )____)____)_____)\\\\
         _____|____|____|____\\\\\\__
---------\ S.S. Market Speed /---------
  ^^^^^ ^^^^^^^^^^^^^^^^^^^^^
    ^^^^      ^^^^     ^^^    ^^
         ^^^^      ^^^"

set -e

if [[ $(git diff .) ]]
then
  echo "You have unstaged changes. Aborting..."
  exit 1
fi

set -x
pushd ui
  npm install
  npm run lint
  npm run sass-lint

  npm run build

  if npm run unit | grep -E "ERROR|FAILED"
  then
    exit 1
  fi

popd

pushd api
  SPRING_PROFILES_ACTIVE=docker ./gradlew clean build dockerApiTest
popd

set +x
echo '
  /$$$$$$                                                            
 /$$__  $$                                                           
| $$  \__/ /$$   /$$  /$$$$$$$  /$$$$$$$  /$$$$$$   /$$$$$$$ /$$$$$$$
|  $$$$$$ | $$  | $$ /$$_____/ /$$_____/ /$$__  $$ /$$_____//$$_____/
 \____  $$| $$  | $$| $$      | $$      | $$$$$$$$|  $$$$$$|  $$$$$$ 
 /$$  \ $$| $$  | $$| $$      | $$      | $$_____/ \____  $$\____  $$
|  $$$$$$/|  $$$$$$/|  $$$$$$$|  $$$$$$$|  $$$$$$$ /$$$$$$$//$$$$$$$/
 \______/  \______/  \_______/ \_______/ \_______/|_______/|_______/ 
                                                                     
                                                                     
                                                                     '