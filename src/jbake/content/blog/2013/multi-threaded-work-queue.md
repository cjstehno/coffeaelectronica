title=Multi-threaded Work Queue
date=2013-01-27
type=post
tags=blog,java,groovy
status=published
~~~~~~
I finally had some time to play with the DataFlow functionality in GPars, and as you would expect, it's both simple and
powerful. I needed a simple means of processing data from a queue in a multi-threaded manner. I considered something like
JMS, but for what I am working on that was too complex and too heavy. I really just needed to toss a message on a queue
and let it get processed at some future time, hopefully by a couple threads. The DataFlowQueue came through perfectly.

Below is a simple demo:

```groovy
def pool = new DefaultPool(false, 3)
def queue = new DataflowQueue<String>()

def latch = new CountDownLatch(100)

queue.wheneverBound(new DataCallbackWithPool(pool,{msg->
    println "[${Thread.currentThread().name}] ${System.currentTimeMillis()} $msg"
    latch.countDown()
}))

100.times { n->
    queue.bind("Message-$n")
}

latch.await()
```

The `DataFlowQueue` accepts messages that it will hand off to bound handlers. The `wheneverBound()` method will bind the
handler permanently (rather than for just a single message) so that you can use the `DataCallbackWithPool` message stream
as a handler. Configure the `DataCallbackWithPool` object with a pool and your actual message handler then you are done.

When you run the demo code you will see that the bind call returns immediately and that each message is handled on one
of the three threads configured.

Nothing Earth-shattering, but it will come in handy. Also, since the above example was written in Groovy, you could also
do this with GPars in Java, with just a few changes:

```java
Pool pool = new DefaultPool( false, 3 );
DataflowQueue<String> queue = new DataflowQueue<>();

final CountDownLatch latch = new CountDownLatch( 100 );

queue.wheneverBound( new DataCallbackWithPool( pool, new MessagingRunnable<String>(){
    @Override
    protected void doRun( final String msg ){
        System.out.printf( "[%s] %d%n", Thread.currentThread().getName(), System.currentTimeMillis() );
        latch.countDown();
    }
}));

for( int i=0; i<100; i++){
    queue.bind( "Message-" + i );
}

latch.await();
```

The main difference being the introduction of the MessageRunnable, since Java does not have closures yet.
