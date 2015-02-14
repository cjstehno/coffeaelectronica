title=Rendering Calendars
date=2008-05-31
type=post
tags=blog,java
status=published
~~~~~~
I am poking around with an idea I had to generate desktop background images with little calendars embedded in them and I
realized that there is no really easy method of rendering your standard square text calendar display. I decided to play
around with it a bit. The basic algorithm to generate the weeks and their associated days is nothing too complicated:

1. Figure out what day of the week the first day of the month falls on.
2. Subtract from that the value for Sunday (the start of the week), which assumes that the day of the week field values are sequential (which they are).
3. Iterate through the day of the month keeping track of the week breaks.

I created a factory class, `MonthFactory` and a couple helper model objects, `Month` and `Week` to make things a little
easier. `Month` and `Week` are pretty simple data objects used to store the month and week data in a meaningful structure.

```java
public class Week {

    private final String[] days = new String[7];
    private int index = 0;

    public Iterable<string> days(){
        return Arrays.asList(days);
    }

    boolean append(final String day){
        days[index++] = day;
        return index != 7;
    }

    void padding(final int size){
        index = size;
    }

    @Override
    public String toString(){
        return Arrays.toString(days);
    }
}
```

I used strings to store the day values, you could also use ints.

```java
public class Month {

    private final List<Week>; weeks = new LinkedList<Week>();
    private final String name;
    private final int year;

    Month(final String name, final int year){
        this.name = name;
        this.year = year;
    }

    public Iterable<Week> weeks(){
        return weeks;
    }

    public String getName(){return name;}

    public int getYear(){return year;}

    void append(final Week week){
        weeks.add(week);
    }

    @Override
    public String toString() {
        return "{" + name + " " + year + ": weeks=" + weeks.toString();
    }
}
```

Both of these classes have very limited public interfaces. They exist in the same package as the `MonthFactory` class which
is used to build them. External client classes should really only be accessing them as read-only. The `MonthFactory` is also
pretty simple, but it does contain the algorithm I mentioned earlier so it is the meat of the whole example:

```java
public class MonthFactory {

    public static Month create(final int calendarMonth, final int year) throws Exception {
        validate(calendarMonth,year);

        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, calendarMonth);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.YEAR, year);

        final int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        final String name = cal.getDisplayName(Calendar.MONTH,Calendar.LONG,Locale.getDefault());
        final Month month = new Month(name,year);

        Week week = new Week();
        week.padding(cal.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY);

        for(int i=1; i<=daysInMonth; i++){
            if(!week.append(String.valueOf(i))){
                month.append(week);
                week = new Week();
            }
        }

        month.append(week);

        return month;
    }

    private static void validate(final int calMon,final int year){
        if(calMon < Calendar.JANUARY || calMon > Calendar.DECEMBER){
            throw new IllegalArgumentException("Invalid Calendar Month specified: " + calMon);
        }

        if(year < 0){
            throw new IllegalArgumentException("Year must be non-negative: " + year);
        }
    }
}
```

I even threw in some simple input validation for free. Now for a simple use of this code I created a `TextCalendar` class
that simply generates a text calendar which will look right if it's rendered in a fixed-width font.

```java
public class TextCalendar {
    public static void main(final String[] args) throws Exception {
        final Month month = MonthFactory.create(Calendar.JULY,2008);
        final StringBuilder str = new StringBuilder();
        final String header = month.getName() + " " + String.valueOf(month.getYear());
        str.append(padding(10 - header.length()/2,' ')).append(header).append('\n');

        for(final Week week : month.weeks()){
            for(final String day : week.days()){
                if(day == null){
                    str.append("  ");
                } else {
                    str.append(day.length() == 2 ? day : " " + day);
                }
                str.append(" ");
            }
            str.append('\n');
        }

        System.out.println(str.toString());
    }

    private static String padding(final int size, final char val){
        final char[] pad = new char[size];
        Arrays.fill(pad, val);
        return new String(pad);
    }
}
```

This just serves to show how easy this makes calendar rendering. Upon running this little application you will get the
following nicely formatted calendar (I even centered the header):

```
     July 2008
       1  2  3  4  5
 6  7  8  9 10 11 12
13 14 15 16 17 18 19
20 21 22 23 24 25 26
27 28 29 30 31
```

If there are existing libraries to do this, I would love to hear about them. I could not find anything. Obviously this
example corresponds to the standard US display of the Gregorian Calendar. I would think that it could be made to work
for other calendars as well, but I have never actually seen any other calendars displayed... I guess I need to travel
the world more. I provide these as examples only; there may be better ways to accomplish this and there are definitely
code modifications and improvements that could be made. I am not currently using this code anywhere... it was just an
experiment. Use at your own risk.
