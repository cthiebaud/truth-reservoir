#!/usr/bin/env zsh
mvn clean package appengine:deploy -Dapp.deploy.projectId="aletheia-8c78f"
