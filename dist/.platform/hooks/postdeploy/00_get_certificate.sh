#!/usr/bin/env bash
# Place in .platform/hooks/postdeploy directory
sudo certbot -n -d truth-reservoir-env.eba-3iuiqiva.eu-north-1.elasticbeanstalk.com --nginx --agree-tos --email christophe.t60@gmail.com
