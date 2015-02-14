title=From Junk Box to Jukebox in a Couple Hours
date=2010-10-17
type=post
tags=blog,java,testing,mocking
status=published
~~~~~~
I finally got tired of griping about not having enough space on my phone to store all of my music, and not wanting to
pay for one of the music cloud storage services... so I decided to bite the bullet and setup my own using
[Ubuntu Server](http://ubuntu.com/), [SubSonic Music Streamer](http://subsonic.org/) and its
Android app. It only took a couple hours, most of which was baby-sitting installations, and now I am able to listen to
any/all of my music whenever and wherever I want; it's quite nice.

The steps are pretty straight forward, but going into this process I must make it clear that I am not an expert network/server administrator,
I am a developer, therefore if this process causes you or your equipment any harm in any way, sorry, but it's not my
fault. Continue at your own risk.

First, you need a computer to work with. I had an old Dell desktop that is about four or five years old and it is working nicely.
Hook up a keyboard, mouse and monitor to the box... if it's an
old desktop box you should have all the connections you need. You will also need to connect the box to your
network.

It almost goes without saying that you will need internet access of at least DSL, but really I would not do this with
anything less than a high-speed cable internet connection.

Download [Ubuntu 10.10 Server](http://www.ubuntu.com/server) (or whatever the most current version is when
you read this). Make sure that you are downloading the server version, _not_ the desktop. Also make sure that you
download the appropriate version for your system, 32- or 64-bit. I had to use 32-bit since it was an old desktop
box.

Burn the .iso file you downloaded to a CD (I recommend [ImgBurn](http://www.imgburn.com/) if
you don't have a favorite image buring application).

Once you have the CD created, pop it into the box you are
building and boot from it. You may have to fiddle with your boot order in your bios, but the CD is bootable. Once the
Ubuntu installation menu comes up, just follow the instructions.

A couple items to be aware of: You will want
to enable the automatic security updates so that you don't have to bother with it yourself. Also, you will come to a
server installation screen (after the restart I think)... at this point I selected LAMP server, Tomcat server and
OpenSSH server. The SSH server is required for this, though the others are not; I plan on adding more to this box so I
wanted to have the servers ready to go.

Once the installation is done, disconnect the keyboard, mouse and
monitor, but leave the box running. You now have a nice clean Linux web server to play with. I recommend giving the new
server box a dedicated IP address on your network so that you don't need to keep track of it. This is gererally very
specific to your router, so I can't really go into it here.

From your desktop box, laptop, whatever, connect to
the new server using your favorite SSH client (for windows you can use [Cygwin](http://cygwin.com/) or
[Putty](http://www.chiark.greenend.org.uk/~sgtatham/putty/)).

All of my music is on a NAS on my
local network, so I had to create a mount of the music directory on the server box. There is a nice wiki article about
how to do this, [Mount Windows Shares Permanently](https://wiki.ubuntu.com/MountWindowsSharesPermanently) so
that's easy enough.

Now download the deb installer file from [SubSonic](http://www.subsonic.org/) and follow the installation
instructions. There are also some additional packages they recommend installing. I did. I eventually want to have
SubSonic use the default servers on the box, but this installation method seemed to be the fastest one to get a server
up and running.

Once you have SubSonic installed you will need to create a directory for your playlists,
"/var/playlists" by default. Now that the server is running, go ahead and login and change the admin password and set
the music and playlist locations. I created a second user to user for myself that did not have admin permissions. Make
sure that you can see a list of your music artists down the left hand side and you are ready to go... at least for
access on your own network.

In order to have access to SubSonic from the outside world, you need to setup port forwarding on your router to map one
of the external ports to the server and port that your SubSonic instance is running on. Again, this is router-specific,
but [Lifehacker](http://lifehacker.com/) has a good overview in the article "[How to Access a Home Server Behind a Router Firewall](http://lifehacker.com/127276/geek-to-live--how-to-access-a-home-server-behind-a-routerfirewall)".

Once you have access from the outside world, you can
install the Android or iPhone apps for SubSonic and play your music while you are away from home. The Android app is
quite good. I created two profiles, one for home when I can use my local wireless network, and another for access when I
am away from my network... helps to keep the bandwidth down. Using the phone is also a nice way to test out the external
interface. Turn off your wifi antenna and connect via 3G.

One concern I have about running my own server is
power consumption. The box I am using was a desktop box and not really made to conserve energy. I am looking into ways
of setting up smart power usage, sleeping, etc, but that will come in another posting. For now I am going to test out
the actual power consuption using one of those [Killawatt](http://www.thinkgeek.com/gadgets/travelpower/7657/) plug-in wattage meters so that I can see
what I am doing to my monthly power bill. One thought I had is that if the power consumption is too high, I might look
into building a really low-power box with an atom processor and solid-state drive or something else really bare bones
and cheap. I have a few other projects in mind for this now that I have a server running, though I don't think I will be
moving my blog hosting there any time soon, but you never know.

If any of this is too vague, please let me know
and I can add more detail. This is intended for users with minimal Linux, networking, and hardware experience, but
sometimes I take more for granted than I realize.

Good luck and have fun!

## Follow-up

After about six days for power monitoring, the server used 6.38 kilowatt-hours of power. With a little groovy calculating:

```groovy
def kwh = 6.38, kwhcost = 0.12
def duration /* days */ = (((time('10/22/2010 06:54') - time('10/16/2010 10:10')) / 1000) / 3600) / 24
def costPerDay = (kwh * kwhcost)/duration
def costPerMonth = costPerDay * 30
def time( d ){ new java.text.SimpleDateFormat('MM/dd/yyyy HH:mm').parse( d ).getTime()}
def money( m ){ new java.text.DecimalFormat('$0.00').format( m )}
println "${money(costPerDay)}/day ==> ${money(costPerMonth)}/month"
```

I found that the server uses roughly $0.13/day or $3.93/month on average based on the time duration I tested in which
the server was under normal usage. Obviously, this number is very dependent on the system you build your server with.
Given some of the new Atom-based boxes that are available, you could probably really cut that cost down even more. If
you consider that most hosting with any useful functionality is at least $3-5 per month, and a lot of the cloud-based
services that are coming out are in the range of $5-10 per month... $3-5 per month for your own server with unlimited
possibilities is not bad at all. Also there is one cost-cutting measure you can do with your own server that you
cannot do with hosting... turn it off.

Finally, I also found a useful tool for working with the server. If you
have an Android phone you can install [ConnectBot](http://code.google.com/p/connectbot/) which gives you SSH
shell access to your server. I don't have my SSH port forwarded from the router so this only works inside my home
network via wireless, but ConnectBot makes it very easy to check on the status of the box, do a shutdown or restart the
web server without having to fire up your desktop box.
