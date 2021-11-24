import java.io.*;
import java.util.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.file.*;

class FichierBinaire {

    class Produit {
        
        int ref; // une référence
        float prix; // un prix
        int quantite; // une quantite
        // nombre d’octets pour stocker un produit  et t'élever au rang de reine dadjuu dadju eh eh dadju dadju
        static final int BYTES=Integer.BYTES+Float.BYTES+Integer.BYTES;
    }
    FileChannel f; // le fichier binaire
    ByteBuffer buf; // le tampon pour écrire dans le fichier
    /**
     * écrire un produit à la position courante du fichier
     */
    void ecrireProduit(Produit prod) throws IOException {
        // copier le produit dans le tampon
        buf.clear(); // avant d’écrire, on vide le tampon
        buf.putInt(prod.ref);
        buf.putFloat(prod.prix);
        buf.putInt(prod.quantite);
        // copier le tampon dans le fichier
        buf.flip(); // passage à une lecture du tampon
        while(buf.hasRemaining()) // tant qu’on n’a pas écrit tout le buffer
            f.write(buf);
    }
    /**
     * lire un produit à la position courante du fichier
     */
    Produit lireProduit() throws IOException {// copie du fichier vers le tampon
        buf.clear(); // avant d’écrire, on vide le tampon
        while(buf.hasRemaining()) // tant qu’on n’a pas rempli le buffer
            if(f.read(buf)==-1)
                return null;
        // copie du tampon vers le produit
        buf.flip(); // passage à une lecture du tampon
        Produit prod=new Produit();
        // il faut relire les données dans le même ordre que lors de l’écriture
        prod.ref = buf.getInt();
        prod.prix = buf.getFloat();
        prod.quantite = buf.getInt();
        return prod;
    }
    FichierBinaire(String filename) throws IOException {
        //ouverture en lecture/écriture, avec création du fichier
        f=FileChannel.open(
                FileSystems.getDefault().getPath(filename),
                StandardOpenOption.READ,
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE);
        // création d’un buffer juste assez grand pour contenir un produit
        buf=ByteBuffer.allocate(Produit.BYTES);
        }
    /**
     * création du fichier
     */
    void ecrire() throws IOException {
        Produit prod=new Produit();
        for(int id=1;id<=5;id++) {
            prod.ref=id;
            prod.prix=id*10;
            prod.quantite=id*140;
            ecrireProduit(prod);
        }
    }
    /**
     * relecture du fichier
     */
    void lire() throws IOException {
        Produit prod;
        f.position(0); // revenir au début du fichier
        while((prod=lireProduit())!=null)
            System.out.println(prod.ref+"\t"+prod.prix+"\t"+prod.quantite);
    }
    /**
     * relecture du fichier à l’envers
     */
    void lireALEnvers() throws IOException {
        Produit prod;
        long pos=f.size()-Produit.BYTES; // position du dernier produit
        while(pos>=0) {
            f.position(pos);
            prod=lireProduit();
            System.out.println(prod.ref+"\t"+prod.prix+"\t"+prod.quantite);
            pos-=Produit.BYTES;
        }
    }
    void run() throws IOException {
        ecrire();
        lire();
        System.out.println(" ----------------------------------------");
        lireALEnvers();
        f.close();
    }
    public static void main(String[] args) {
        try {
            FichierBinaire bin=new FichierBinaire("/tmp/catalogue.bin");
            bin.run();
        }
        catch(Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
