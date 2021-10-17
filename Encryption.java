import java.awt.image.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.imageio.*;
/**
 * This class computes the encryption of the message and the GUI .
 *
 * @author (Michael Bienasz)
 * @version (7/28/2021)
 */
public class Encryption extends JFrame implements ActionListener
{
    JButton open = new JButton("Open Picture");
    JButton encrypt = new JButton("Encrypt Message");
    JButton save = new JButton("Save message");
    JButton steps = new JButton("Help");
    JScrollPane originalPane = new JScrollPane();
    JTextArea message = new JTextArea(10,5);
    // GUI variables

    BufferedImage sourceImage = null;  
    BufferedImage embeddedImage = null;
    String k1;
    // Extra variables for the original/ steganographic pictures as well as key for authenticating

    //Constructer
    public Encryption() {
        super("Encrypt a message");
        assembleInterface();
        this.setSize(500,500);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE); 
        this.setVisible(true);
        this.validate();
    }

    //Assembles the GUI interface
    private void assembleInterface() {
        JPanel p = new JPanel(new FlowLayout());
        p.add(open);
        p.add(encrypt);
        p.add(save); 
        p.add(steps);
        p.setBackground(Color.BLUE);
        this.getContentPane().add(p, BorderLayout.NORTH);
        open.addActionListener(this);
        encrypt.addActionListener(this);
        save.addActionListener(this);   
        steps.addActionListener(this); 

        p = new JPanel(new GridLayout(1,1));
        p.add(new JScrollPane(message));
        message.setFont(new Font("Times New Roman",Font.BOLD,20));
        p.setBorder(BorderFactory.createTitledBorder("Enter Message"));
        this.getContentPane().add(p, BorderLayout.SOUTH);

        originalPane.setBorder(BorderFactory.createTitledBorder("Original Image"));
        this.getContentPane().add(originalPane, BorderLayout.CENTER);
    }
    // Control panel based on user input
    public void actionPerformed(ActionEvent ae) {
        Object o = ae.getSource();
        if(o == open)
            openImage();
        else if(o == encrypt)
            embedMessage();
        else if(o == save) 
            saveFile();
        else if(o == steps) 
            steps();
    }
    //Incorperates File Chooser to select the image to preform steganongraphy

    private java.io.File showFileDialog(final boolean open) {
        JFileChooser fc = new JFileChooser("Open an image");
        javax.swing.filechooser.FileFilter ff = new javax.swing.filechooser.FileFilter(){ 

                public boolean accept(java.io.File f) {
                    String name = f.getName().toLowerCase();
                    if(open)
                        return f.isDirectory() || name.endsWith(".jpg") || name.endsWith(".bmp") ||
                        name.endsWith(".png"); 

                    return f.isDirectory() || name.endsWith(".png") ||    name.endsWith(".bmp");
                }

                public String getDescription() {
                    if(open)
                        return "Image (*.jpg, *.jpeg, *.png, *.gif, *.tiff, *.bmp, *.dib)";
                    return "Image (*.png, *.bmp)";
                }
            };
        fc.setAcceptAllFileFilterUsed(false);
        fc.addChoosableFileFilter(ff);

        java.io.File f = null;
        if(open && fc.showOpenDialog(this) == fc.APPROVE_OPTION)
            f = fc.getSelectedFile();
        else if(!open && fc.showSaveDialog(this) == fc.APPROVE_OPTION)
            f = fc.getSelectedFile();
        return f;
    }

    //GUI method that opens the image and displays it on screen
    private void openImage() {
        java.io.File f = showFileDialog(true);
        try {   
            sourceImage = ImageIO.read(f);
            JLabel l = new JLabel(new ImageIcon(sourceImage));
            originalPane.getViewport().add(l);
            this.validate();
        } catch(Exception ex) { ex.printStackTrace(); }
    }

    //Method that copies the encoding of the steganographic image
    private void embedMessage() {
        String m = message.getText();
        embeddedImage=sourceImage;
        encode(embeddedImage, m);

    }
    //Method preforms encoding of the text
    public boolean encode(BufferedImage img, String message)
    {
        return add_text(img,message);
    }

    // Add text to the image bytes
    private boolean add_text(BufferedImage image, String text)
    {

        //3 byte arrays of data are needed that holds the image, message, and length of message bytes 
        byte img[]  = get_byte_data(image);
        byte msg[] = text.getBytes();
        byte len[]   = bit_conversion(msg.length);
        // 
        try
        {
            encode_text(img, len,  0); //Call method to encode image length to picure
            encode_text(img, msg, 32); // Call method to encode message 32 bits
        }
        catch(Exception e)
        {
            JOptionPane.showMessageDialog(null, "Target File cannot hold message!", "Error",JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    //Convert pixel data from image into bytes to be used in encoding message 
    private byte[] get_byte_data(BufferedImage image)
    {
        WritableRaster raster   = image.getRaster();
        DataBufferByte buffer = (DataBufferByte)raster.getDataBuffer();
        return buffer.getData();
    }

    //Utility method that converts the integer that hold the image length to byte format 
    private byte[] bit_conversion(int i)
    {
        byte byte3 = (byte)((i & 0xFF000000) >>> 24);
        byte byte2 = (byte)((i & 0x00FF0000) >>> 16);
        byte byte1 = (byte)((i & 0x0000FF00) >>> 8 );
        byte byte0 = (byte)((i & 0x000000FF)       );
        return(new byte[]{byte3,byte2,byte1,byte0});
    }

    //Method that authenticates user that decrypts the steganographic image
    public String getKey()
    {
        k1=JOptionPane.showInputDialog("Enter a Key to authenticate decryption" );
        return k1;

    }

    //Method that uses the LSB formula to encode the message bytes with in the 
    // the image pixels
    private byte[] encode_text(byte[] image, byte[] key, int offset)
    {
        //Check to see if image is a big enough file to hold the message bytes
        if(key.length + offset > image.length)
        {
            throw new IllegalArgumentException("File not long enough!");
        }
        //Loop through each byte of the key array
        for(int i=0; i<key.length; ++i)
        {
            int add = key[i]; //Assign the add integer to be the current byte
            for(int bit=7; bit>=0; --bit, ++offset) // Loops through each bit stored in byte
            {
                int b = (add >>> bit) & 1; // b gets assigned the value the add shifted right and 1
                image[offset] = (byte)((image[offset] & 0xFE) | b ); // Change the LSB of the image byte to be the bit to the add bit
            }
        }
        return image;
    }

    //Saves the encrypted image file and creates a new one with png extension
    private void saveFile() {
        if(embeddedImage == null) {
            JOptionPane.showMessageDialog(this, "No message has been embedded!", 
                "Nothing to save", JOptionPane.ERROR_MESSAGE);
            return;
        }
        java.io.File f = showFileDialog(false);
        String name = f.getName();
        String ext = name.substring(name.lastIndexOf(".")+1).toLowerCase();
        if(!ext.equals("png") && !ext.equals("bmp") &&   !ext.equals("dib")) {
            ext = "png";
            f = new java.io.File(f.getAbsolutePath()+".png");

        }

        try {
            if(f.exists()) f.delete();
            ImageIO.write(embeddedImage, ext.toUpperCase(), f);
        } catch(Exception ex) { ex.printStackTrace(); }
    }
    // Steps for encryption
    private void steps()
    {
        JOptionPane.showMessageDialog(null,"Open image file to be encrypted", "Step 1", JOptionPane.INFORMATION_MESSAGE);
        JOptionPane.showMessageDialog(null,"Type in a message in text box", "Step 2", JOptionPane.INFORMATION_MESSAGE);
        JOptionPane.showMessageDialog(null,"Once full message is typed, Press encrypt", "Step 3", JOptionPane.INFORMATION_MESSAGE);
        JOptionPane.showMessageDialog(null,"Save and create the steganographic image file ", "Step 4", JOptionPane.INFORMATION_MESSAGE);

        

    }
    public static void main(String args[]) {
        new Encryption();
    }
}

