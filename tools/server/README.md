# Server installation

## Java IS required :-)
`sudo apt-get update && sudo apt-get install oracle-java8-jdk`

## Create directories needed for application
`mkdir /home/pi/smart-house && mkdir /home/pi/smart-house/run && mkdir /home/pi/smart-house/log`

## Start the service at server start
`sudo update-rc.d smart-house defaults`

## Set the correct timezone
`sudo dpkg-reconfigure tzdata` or even better `sudo timedatectl set-timezone Europe/Ljubljana`

## Restart every morning at 4 AM
`(sudo crontab -l 2>/dev/null; echo "0 4 * * * /etc/init.d/smart-house restart") | sudo crontab -`

## Start the smart house
`sudo chmod +x /etc/init.d/smart-house`
`sudo /etc/init.d/smart-house start`

## Import the configuration

Export variables from "tools/variables" first: `source tools/variables`

Then:
`curl -X POST http://$SH_HOST:$SH_PORT/import?fileName=/home/pi/smart-house/konfiguracija-hisa.xlsx -u $SH_USERNAME:$SH_PASSWORD`


# FTP server on USB

###   1. create folder for mount
`sudo mkdir /mnt/usb`

###   2. get the USB UUID
`ls -l /dev/disk/by-uuid/`

###   3. add the line at the end of the file to mount the USB at reboot
`sudo sed -i '$a UUID={uuid-from-previous-step} /mnt/usb ext4 rw 0 3' /etc/fstab`

###   4. try if it mounts
`sudo mount /mnt/usb`

# FTP server

`sudo apt-get install pure-ftpd`

We need to create a new user group named ftpgroup and a new user named  ftpuser for FTP users, and make sure this "user" has NO log in privilge and NO home directory:

`sudo groupadd ftpgroup`

`sudo useradd ftpuser -g ftpgroup -s /sbin/nologin -d /dev/null`

Make a new directory named `ftp` for the user:

`sudo mkdir /mnt/usb/ftp`

Make sure the directory is accessible for `ftpuser` user:

`sudo chown -R ftpuser:ftpgroup /mnt/usb/ftp`

Create a virtual user named upload, mapping the virtual user to ftpuser and  ftpgroup, setting home directory /home/pi/FTP, and record password of the user in database
- user: `upload`
- pass: `camupload`

`sudo pure-pw useradd upload -u ftpuser -g ftpgroup -d /mnt/usb/ftp -m`

Set up a virtual user database:

`sudo pure-pw mkdb`

Define an authentication method by making a link of file  /etc/pure-ftpd/conf/PureDB, the number 60 is only for demonstration, make it as small as necessary:

`sudo ln -s /etc/pure-ftpd/conf/PureDB /etc/pure-ftpd/auth/60puredb`

Restart the ftp server:

`sudo service pure-ftpd restart`

Test it with an FTP client, like FileZilla

Update the "motion.detection.path" with path from above:

`sudo nano /etc/smart-house`


# Bluetooth sound card

Edit `/etc/modprobe.d/alsa-base.conf` and put lines below to the file

```
## This sets the index value of the cards but doesn't reorder.
options snd_usb_audio index=0
options snd_bcm2835 index=1

## Does the reordering.
options snd slots=snd_usb_audio,snd_bcm2835
```

List sound cards:
`cat /proc/asound/modules`

### Record sound

`arecord [-D plughw:0,0] -q -t wav -d 0 -f S16_LE -r 16000temp.wav`

`arecord -q -t wav -d 0 -f S16_LE -r 16000 temp.wav`

### Play sound
`aplay [-D plughw:0,0] temp.wav`


## Edit `/usr/share/alsa/alsa.conf`

Change `defaults.ctl.card` and `defaults.pcm.card` to the same slot as above

Edit `~/.asoundrc` and put lines below to the file:

```
pcm.!default {
  type asym
   playback.pcm {
     type plug
     slave.pcm "hw:0"
   }
   capture.pcm {
     type plug
     slave.pcm "hw:0"
   }
}
```

OR this one:
```
pcm.!default {
  type plug
  slave {
    pcm "plughw:0,0"
    format S16_LE
  }
}
```
