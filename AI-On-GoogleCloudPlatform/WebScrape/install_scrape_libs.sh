#!/bin/bash
pip install webdriverdownloader
pip install -U -q selenium
pip install selenium-wire
pip install opencv-contrib-python
pip install orderedset
pip install xmltodict
pip install xlrd
pip install keras
apt-get update
apt-get install -y unzip xvfb libxi6 libgconf-2-4 -y --allow-unauthenticated
apt-get install default-jdk -y --allow-unauthenticated
curl -sS -o - https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add
echo "deb [arch=amd64]  http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google-chrome.list
apt-get -y update
apt-get -y install google-chrome-stable --allow-unauthenticated
wget https://chromedriver.storage.googleapis.com/2.41/chromedriver_linux64.zip
unzip chromedriver_linux64.zip
mv chromedriver /usr/bin/chromedriver
chown root:root /usr/bin/chromedriver
chmod +x /usr/bin/chromedriver
wget https://selenium-release.storage.googleapis.com/3.13/selenium-server-standalone-3.13.0.jar
wget http://www.java2s.com/Code/JarDownload/testng/testng-6.8.7.jar.zip
unzip testng-6.8.7.jar.zip
rm *.zip
rm *.jar

