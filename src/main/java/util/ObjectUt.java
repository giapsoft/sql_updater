package util;

public class ObjectUt {
    public static <T> T firstNonNull(T... list) {
        for(T t : list) {
            if(t != null) {
                return t;
            }
        }
        return null;
    }

    public static boolean equalPairs(Object... objects) {
        for(int i = 0; i < objects.length; i++) {
            if (i % 2 == 0) {
               Object a = objects[i];
               Object b = objects[i + 1];
               if(a instanceof String || b instanceof String) {
                   assert a instanceof String;
                   if(!StringUt.areEqual((String) a, (String) b)){
                       return false;
                   }
               } else {
                   if (a != b) {
                       if(a == null  || b == null) {
                           return false;
                       }
                       if(!a.equals(b)) {
                           return false;
                       }
                   }
               }
            }
        }
        return true;
    }
}
