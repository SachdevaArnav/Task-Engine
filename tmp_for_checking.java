import java.util.Scanner;

public class tmp_for_checking {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        String query = input.nextLine();
        String[] Tokens = query.split(" ");
        for(String token:Tokens){
       System.out.println(token);
 }
}
}
