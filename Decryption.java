import java.awt.image.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.imageio.*;
/**
 * This class includes the decryption algorithm and the GUI for the application.
 *
 * @author (Michael Bienasz)
 * @version (8/1/2021)
 */
public class Decryption  extends JFrame implements ActionListener
{
    JButton open = new JButton("Open");
    JButton decode = new JButton("Decode");
    JButton steps = new JButton("Help");
    JScrollPane imagePane = new JScrollPane();
    JTextArea message = new JTextArea(10,5);
    // GUI 

    BufferedImage image = null;
    // Final image that extracts message

    // Constructer
    public Decryption() {
        super("Decode stegonographic message in image");
        assembleInterface();
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);   
        this.setSize(500,500);
        this.setVisible(true);
    }

    //Assemble GUI
    private void assembleInterface() {
        JPanel p = new JPanel(new FlowLayout());
        p.add(open);
        p.add(decode);
        p.add(steps);
        p.setBackground(Color.BLUE);
        this.getContentPane().add(p, BorderLayout.NORTH);
        open.addActionListener(this);
        decode.addActionListener(this);
        steps.addActionListener(this);

        p = new JPanel(new GridLayout(1,1));
        p.add(new JScrollPane(message));
        message.setFont(new Font("Arial",Font.BOLD,20));
        p.setBorder(BorderFactory.createTitledBorder("Decoded message"));
        message.setEditable(false);
        this.getContentPane().add(p, BorderLayout.SOUTH);

        imagePane.setBorder(BorderFactory.createTitledBorder("Steganographed Image"));
        this.getContentPane().add(imagePane, BorderLayout.CENTER);
    }

    //Action control
    public void actionPerformed(ActionEvent ae) {
        Object o = ae.getSource();
        if(o == open)
            openImage();
        else if(o == decode)
            decodeMessage();
        else if(o == steps)
            steps();
    }
    // File dialog only accepts png and bmp format
    private java.io.File showFileDialog(boolean open) {
        JFileChooser fc = new JFileChooser("Open an image");
        javax.swing.filechooser.FileFilter ff = new javax.swing.filechooser.FileFilter() {
                public boolean accept(java.io.File f) {
                    String name = f.getName().toLowerCase();
                    return f.isDirectory() ||   name.endsWith(".png") || name.endsWith(".bmp");
                }

                public String getDescription() {
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
    // Dusplay image in GUI
    private void openImage() {
        java.io.File f = showFileDialog(true);
        try {   
            image = ImageIO.read(f);
            JLabel l = new JLabel(new ImageIcon(image));
            imagePane.getViewport().add(l);
            this.validate();
        } catch(Exception ex) { ex.printStackTrace(); }
    }
    // Decode function includes authentication trigger 
    public String decode(BufferedImage img)
    {
        JOptionPane.showMessageDialog(null, "You must authenticate first!");
        Encryption key1= new Encryption();
        String userkey =key1.getKey();
        String dkey=JOptionPane.showInputDialog("Enter Key" );
        //Calls encryption class gets key from encrypter, and checks if its correct
        if(dkey.equals(userkey))
            JOptionPane.showMessageDialog(null,
                "Authorized", "Key", JOptionPane.INFORMATION_MESSAGE);
        else
            JOptionPane.showMessageDialog(null, 
                "Wrong Key!","Error",
                JOptionPane.ERROR_MESSAGE);
        
        byte[] decode; //
        try
        {

            // Computes decoded bits from image and returns a string of the message
            decode = decode_text(get_byte_data(image));
            return(new String(decode));
        }
        catch(Exception e)
        {
            JOptionPane.showMessageDialog(null, 
                "There is no hidden message in this image!","Error",
                JOptionPane.ERROR_MESSAGE);
            return "";
        }
    }
   
    
    //Setter method 
    private void decodeMessage() {
        message.setText(decode(image));
    }
    // Converts pixel data into a byte array
    private byte[] get_byte_data(BufferedImage image)
    {
        WritableRaster raster   = image.getRaster();
        DataBufferByte buffer = (DataBufferByte)raster.getDataBuffer();
        return buffer.getData();
    }
    // Method includes decryption algorithm 
    private byte[] decode_text(byte[] image)
    {
        int length = 0;
        int offset  = 32;
        //Loop to figure out length of the message
        for(int i=0; i<32; ++i) 
        {
            length = (length << 1) | (image[i] & 1);
        }
        //Store length of text into byte array
        byte[] result = new byte[length];

        //loop through all bytes of text
        for(int b=0; b<result.length; b++ )
        {
            //loop through each bit
            for(int i=0; i<8; ++i, ++offset)
            {
                //The LSB of the message bytes are computed and stored in the result 
                result[b] = (byte)((result[b] << 1) | (image[offset] & 1));
            }
        }
        return result;
    }
    // Provides the steps needed to decode
    private void steps()
    {
        JOptionPane.showMessageDialog(null,"Open the image to be decrypted", "Step 1", JOptionPane.INFORMATION_MESSAGE);
        JOptionPane.showMessageDialog(null,"Press decode to recive the message sent", "Step 2", JOptionPane.INFORMATION_MESSAGE);
        


    }


    
    public static void main(String arg[]) {
        new Decryption();
    }
}
