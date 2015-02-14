title=Home Server v2
date=2011-10-15
type=post
tags=blog,javascript
status=published
~~~~~~
Since my [last home server](From-Junk-Box-to-Jukebox-in-a-Couple-Hours) was fried by a lightning storm a few months back,
I have been serverless, which is an uncomfortable position for a server software developer. I did some good research and decided to try and put together a new box from scratch. My
previous server was just a converted "retired" desktop box I had so this would be a fun project.

[Lifehacker](http://lifehacker.com/) put out a timely series of articles about how to build your own
computer. It was an awesome introduction for someone like myself who had never done more than swap out some RAM or a
hard drive. If you are a computer-build noob (like me), I would highly recommend giving the series a read
([How to Build a Computer from Scrath: A Complete Guide](http://lifehacker.com/5828747/how-to-build-a-computer-from-scratch-the-complete-guide))
if you are thinking of putting together your own box.

For my home server I had only a few requirements:

* Low power consumption - this is running at home, I don't want to overly-inflate my electrical bill.
* Low price - this is a home server box for my projects and some helpful apps for my wife, it needs to be cheap.
* Room to grow - something with some upgrade room would be nice

What I was able to put together fit all those criteria nicely:

* [LIAN LI PC-A05NA Silver Aluminum ATX Mini Tower Computer Case](http://www.newegg.com/Product/Product.aspx?Item=N82E16811112219) - I had originally found a cheaper, smaller case; however, the Lifehacker article recommends some brands over others so I spent a few dollars more and I feel that it was worth it. The case has a lot of room for what I have and may want to add in the future. It also has great cooling and even looks nice.
* [Antec EarthWatts EA-500D Green 500W ATX12V v2.3 / EPS12V 80 PLUS BRONZE Certified Active PFC Power Supply](http://www.newegg.com/Product/Product.aspx?Item=N82E16817371035) - Again, I had a cheaper one picked out but deferred to a Lifehacker-recommended brand. It's also a "green" model with greater power efficiency and enough available power to allow for future growth.
* [Intel BOXD525MW Intel Atom D525@1.8GHz (Dual Core) BGA559 Intel NM10 Mini ITX Motherboard/CPU Combo](http://www.newegg.com/Product/Product.aspx?Item=N82E16813121442) - I decided to start with an Intel motherboard with an Atom processor for low power usage and I think it should be enough processor power for my needs (1.8 GHz dual core 64-bit)
* [Kingston ValueRAM 4GB (2 x 2GB) 204-Pin DDR3 SO-DIMM DDR3 1066 (PC3 8500) Laptop Memory Model KVR1066D3SOK2/4GR](http://www.newegg.com/Product/Product.aspx?Item=N82E16820139033&amp;cm_sp=Pers_InterAlsoBought-_-20-139-033_3_DM-_-13-121-442&amp;nm_mc=AFC-C8Junction&amp;cm_mmc=AFC-C8Junction-_-RSSDailyDeals-_-na-_-na&amp;AID=10521304&amp;PID=4165814&amp;SID=2ixej4ic5pa5) - The supported max is 4GB, I bought 4GB.
* [Western Digital Caviar Blue WD800AAJS 80GB 7200 RPM 8MB Cache SATA 3.0Gb/s 3.5" Internal Hard Drive -Bare Drive](http://www.newegg.com/Product/Product.aspx?Item=N82E16822136195&amp;nm_mc=AFC-C8Junction&amp;cm_mmc=AFC-C8Junction-_-RSSDailyDeals-_-na-_-na&amp;AID=10521304&amp;PID=4165814&amp;SID=ppu8hu6z3s) - Your standard Western Digital in the smallest size that I could find (80GB) without going into something too old. My storage requirements are small. I had originally wanted to get a SSD but that would have blown the cost up too much.
* No optical drive, sound card, video card or network card; the latter three all being integrated on the motherboard, the optical drive was not necessary for a server like this.

I bought everything from [NewEgg](http://newegg.com/) for a grand total (with extra warranty and shipping) out of pocket of a hair over
_$300_. Not bad at all considering everything I looked at "off the rack" was at least $400, and usually had more or less
than what I actually wanted. Maybe someone with more experience building systems could do better since there are probably
areas I could have cut corners on where I played it safe.

![The components](https://raw.github.com/cjstehno/coffeaelectronica/master/content/files/compbuild1.jpg)

Once all the parts came in (the following week, damn they ship fast), I put everything together with the help of the
Lifehacker articles. With a little help and guidance it was actually a pretty easy and mostly straight-forward process
that took about three hours. Once everything was secured and wired together, I fired it up for the first time and was
able to get into the BIOS; however, my hard drive was not being recognized. After trying different cables and then
trying the drive in my desktop computer the only thing I can figure is that the drive was dead on arrival, great quality
control WD! I ran out to BestBuy and bought another of the same drive (well the same brand but larger size) for
basically the same price to test my theory and I was correct. Once the new drive was in place everything worked fine. I
returned the dead drive to NewEgg and have gotten my refund with no problems.

![The completed build](https://raw.github.com/cjstehno/coffeaelectronica/master/content/files/compbuild12.jpg)


Next, I moved on to the OS. My server OS of choice is [Ubuntu](http://www.ubuntu.com/) so I downloaded the
64-bit version of Ubuntu 11.04 and followed the instructions to create a USB installer... I have no optical drive,
remember. It was down right simple and it booted from the stick with no problem and installed, as always, like a
dream.

From there I basically followed along with my previous post to install my shared directories and the
applications I wanted. It's been nice having a server up and running again.

I think in a couple years when it's
time to upgrade my desktop, I will do a custom build. It's kind of fun and you get exactly what you want.
