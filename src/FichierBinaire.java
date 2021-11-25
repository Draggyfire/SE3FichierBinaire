import java.io.*;
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
    int rechercherIndexProduit(int ref) throws IOException{
        int index = 1;
        Produit prod;
        f.position(0);
        while((prod=lireProduit())!=null){
            if(prod.ref == ref)return index;
            index++;
        }
        return -1;
    }
    void ajouterProduit(Produit prod) throws IOException {
        int index = rechercherIndexProduit(prod.ref);
        if(index >=0){
           f.position((index-1)*Produit.BYTES);
           ecrireProduit(prod);
        }else {
            f.position(f.size());
            ecrireProduit(prod);
        }
    }
    void nouvelleQuantite(int ref , int quantite) throws IOException {
        int index = rechercherIndexProduit(ref);
        if(index==-1)return;
        f.position((index-1)*Produit.BYTES);
        Produit prod = lireProduit();
        if(prod.quantite+quantite>=0)
            prod.quantite+=quantite;
        f.position((index-1)*Produit.BYTES);
        ecrireProduit(prod);
    }

    void deleteProduit(int ref) throws IOException {
        int index = rechercherIndexProduit(ref);
        if(index == -1)return;
        f.position(f.size()-Produit.BYTES);
        Produit prod = lireProduit();
        f.position((index-1)*Produit.BYTES);
        ecrireProduit(prod);
        f.truncate(f.size()-Produit.BYTES);
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

    public int search(int r) throws IOException {
        Produit prod;
        f.position(0);
        int count=0;
        // revenir au début du fichier
        while ((prod = lireProduit()) != null){
            if(prod.ref ==r){
                return count*Produit.BYTES;
            }else {
                count++;
            }
        }
        return -1;
    }

    void run() throws IOException {
        ecrire();
        lire();
        System.out.println(" ----------------------------------------");
        lireALEnvers();
        System.out.println(" ----------------------------------------");
        Produit p = new Produit();
        p.ref = 1;
        p.quantite=999;
        p.prix= 999;
        ajouterProduit(p);
        nouvelleQuantite(2,-200);
        lire();
        System.out.println(" ----------------------------------------");
        deleteProduit(3);
        lire();
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
