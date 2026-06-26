import java.nio.file.Path;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        TaskEngine taskEngine = new TaskEngine();
        Scanner input = new Scanner(System.in);
        System.out.println("Task Engine Demo");
        System.out.println();
        System.out.println("Demo workflows are provided for quick evaluation");
        System.out
                .println("Each option executes a readable .mol (Molecule) workflow from the TestMolecules/ directory");
        System.out.println("The workflow definitions can be inspected or modified.");
        while (true) {
            System.out.println();
            System.out.println("1. Semantic Document Retrieval");
            System.out.println("--> Search and Open Project Document");
            System.out.println("2. Multi-Window UI Workflow Execution");
            System.out.println("--> Open Notepad, Save As → Cancel");
            System.out.println("3. Browser Navigation & Content Verification");
            System.out.println("--> Open Example.com in Browser and verify");
            System.out.println("Select by entering the serial number. Press 0 to exit");
            int serialNo = input.nextInt();
            if (serialNo <= 0) {
                break;
            }
            try {
                switch (serialNo) {
                    case 1 -> taskEngine.execute(Path.of("TestMolecules/Search_Open_Project_Doc.mol"), input);
                    case 2 -> taskEngine.execute(Path.of("TestMolecules/Notepad_SaveAs_Cancel.mol"), input);
                    case 3 -> taskEngine.execute(Path.of("TestMolecules/Browse_Verify.mol"), input);
                }
                eFriend.voicemsg("Workflow completed successfully");
            } catch (Exception e) {
                eFriend.voicemsg(e.getMessage());
            }
        }
    }

}
