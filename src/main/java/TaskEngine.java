import java.nio.file.Path;
import java.util.Scanner;

public class TaskEngine {
    public void execute(Path moleculePath, Scanner input) throws Exception {
        MoleculeReader moleculeReader = new MoleculeReader();
        Molecule molecule = moleculeReader.read(moleculePath);
        moleculeReader.run(molecule, input);
    }
}
