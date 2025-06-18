import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.LinkedList;
public class search{
     static boolean matchup_literal(String word,String value){
            Pattern pattern = Pattern.compile(word,Pattern.CASE_INSENSITIVE|Pattern.LITERAL);
            Matcher matcher = pattern.matcher(value);
            boolean matchFound = matcher.find();
            return matchFound;
        }
 //this program is for searching files and uses dir command on cmd.exe thus requiring some piping and prasing raw text through BufferReader coupled with limited filtering and security risk of shell injection
        public static void main(String[] args){
        Scanner input=new Scanner(System.in);
        System.out.println("Enter filename to get path");
        String filename=input.nextLine();
        input.close();
        LinkedList <String> paths=new LinkedList<>();
        String path;
        String[] dirs={"D:\\","C:\\Users\\","A:\\"};
        for(int i=0;i<dirs.length;i++){
        try{
                ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c","dir /s /b "+dirs[i]+"*\""+filename+"*\"");
                Process process = processBuilder.start();
                BufferedReader reader=new BufferedReader(new InputStreamReader(process.getInputStream()));
                while((path=reader.readLine())!=null){
                    if(!(matchup_literal("AppData\\",path))&&!(matchup_literal("Roaming\\",path))&&!(matchup_literal("bin\\",path))&&!(matchup_literal("build\\",path))&&!(matchup_literal("cache\\",path))&&!(matchup_literal("logs\\",path))&&!(matchup_literal(".git\\",path))&&!(matchup_literal(".svn\\",path))&&!(matchup_literal(".config\\",path))&&!(matchup_literal(".terraform\\",path))&&!(matchup_literal(".npm\\",path))&&!(matchup_literal("temp\\",path))&&!(matchup_literal("tmp",path))&&!(matchup_literal("gradle\\",path))&&!(matchup_literal(".vscode\\",path))&&!(matchup_literal("release\\",path))&&!(matchup_literal("node_modules",path))&&!(matchup_literal("venv\\",path))&&!(matchup_literal("__pycache__\\",path))){
                         paths.add(path);
                    }
                   
                }
    }
    catch(Exception e){
        System.out.println("Error: "+e);
    }}
    System.out.println(paths);
}
}