import java.util.* ;

public final class SteinerGen {
  public static void main(String[] args) {
    int n = Integer.parseInt(args[0]) ;

    if ( (((n-1)%2) != 0) || (((n*(n-1))%6)!=0))  {
      System.out.println("Invalid n") ;
      return ;
    }

    System.out.println("ESSENCE' 1.0") ;
    System.out.println("letting v be "+n) ;
    System.out.println("letting b be "+((n*(n-1))/6)) ;
    System.out.println("letting r be "+((n-1)/2)) ;
    System.out.println("letting k be 3") ;
    System.out.println("letting l be 1") ;
    System.out.println("letting n be 100") ;
  }
}
