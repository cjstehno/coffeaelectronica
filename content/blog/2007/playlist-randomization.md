title=Playlist Randomization
date=2007-12-21
type=post
tags=blog,java
status=published
~~~~~~
I love [WinAmp](http://winamp.com/); however, I have always felt that it's playlist randomization was a little on the
weak side. Not really wanting to dive into writing a C++ winamp plugin, I took the alternate approach of writing a
[Groovy](http://groovy.codehaus.org/) script to randomize playlist files.

```groovy
// PlaylistRandomizer.groovy
import java.io.File
import java.util.ArrayList
import java.util.Collections
import java.security.SecureRandom

def songs = new ArrayList()
new File(args[0]).eachLine {
    if(!it.startsWith('#')){
        songs << it
    }
}

Collections.shuffle(songs,new SecureRandom())

new File("random_${args[0]}").withWriter { writer->
    songs.each { line->
        writer.writeLine(line)
    }
}
println 'Done.'
```

You execute it with the file name of the playlist you want to shuffle.

```
groovy PlaylistRandomizer rock_n_roll.m3u
```

and it will generate a new, shuffled file, `random_rock_n_roll.m3u`. It's pretty simple and straight-forward. I am sure
that I could spend a bit more time with it and pare it down a bit, but isn't quick simplistic functionality one of the
benefits of scripting languages?

> _Note:_ I used `SecureRandom` instead of just the standard `Random` because it provides better shuffling, though the
difference is not all that significant.

For some fun and practice, I figured I should implement the same script in [Ruby](http://ruby-lang.org/). I was able to do it in
about ten minutes.

```ruby
# rand_playlist.rb
lines = []
File.open("#{ARGV[0]}","r") do |file|
    while(line = file.gets)
        unless line[0..0] == '#'
            lines << line
        end
    end
end

lines.sort! { rand(3) - 1 }

out_file = File.new("random_#{ARGV[0]}","w");
lines.each do |line|
    out_file.puts line
end
```

You run this one the same as the last, except using ruby:

```
ruby rand_playlist.rb rock_n_roll.m3u
```

I wonder if there are other languages I should try implementing this in.
