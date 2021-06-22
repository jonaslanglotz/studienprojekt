package main.java.view.main;

import lombok.Getter;

public class MapViewUpdate {
    @Getter
    final long start;
    @Getter
    final long finish;
    @Getter
    final long duration;

     public MapViewUpdate (long start, long finish) {
         this.start = start;
         this.finish = finish;
         this.duration = finish - start;
     }

}
