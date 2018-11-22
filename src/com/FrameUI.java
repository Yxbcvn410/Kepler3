package com;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.*;
import java.net.URL;
import java.util.Scanner;

class FrameUI extends JFrame {
    private SystemParams Params;
    private JTextArea reqAccu;
    private Canvas canvas;
    private BufferedImage img;
    private Graphics graphics;
    private File imgFile;
    private PrintWriter logWrt;
    private int size;
    private boolean isRunning;
    private JButton startButton;
    private JComboBox com;
    private boolean perfLyapunov;

    FrameUI(int n, boolean rand, boolean lyapunov) {
        perfLyapunov = lyapunov;
        this.setTitle("Kepler v7.1.2");
        try {
            this.setIconImage(ImageIO.read(new File("res/logo.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        isRunning = false;
        size = 800;
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.CENTER;
        gc.gridheight = n + 12;
        gc.gridwidth = 2;
        gc.gridheight = 1;
        gc.gridwidth = 1;
        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 0.5;
        gc.weighty = 0.5;
        gc.insets = new Insets(3, 3, 3, 3);
        gc.fill = GridBagConstraints.BOTH;

        Params = new SystemParams(n, rand);
        if (rand)
            Params.balance();

        com = new JComboBox<Integer>();
        for (int i = 0; i < Params.n; i++)
            com.addItem(i + 1);
        this.add(com, gc);

        gc.gridx = 1;

        JButton setParams = new JButton("Specify planet");
        setParams.addActionListener(arg0 -> {
            Params.setPlanet(com.getSelectedIndex(),
                    getLaunchParams(Params.getPlanet(com.getSelectedIndex()), com.getSelectedIndex() + 1));
            if (!isRunning)
                RefreshPreview();
        });
        this.add(setParams, gc);
        gc.gridy++;
        gc.gridx = 0;
        JButton addParams = new JButton("Add planet");
        addParams.addActionListener(arg0 -> {
            BigDecimal[] pars = getLaunchParams(null, com.getItemCount() + 1);
            if (pars == null)
                return;
            Params.addPlanet(pars);
            com.addItem(com.getItemCount() + 1);
            if (!isRunning)
                RefreshPreview();
        });
        this.add(addParams, gc);
        gc.gridx = 1;

        JButton remParams = new JButton("Remove planet");
        remParams.addActionListener(arg0 -> {
            Params.removePlanet(com.getSelectedIndex());
            com.removeItemAt(com.getItemCount() - 1);
            if (!isRunning)
                RefreshPreview();
        });
        this.add(remParams, gc);
        gc.gridy++;

        gc.gridx = 0;
        gc.gridwidth = 1;
        JLabel ac = new JLabel("Required step accuracy");
        this.add(ac, gc);
        gc.gridx = 1;
        reqAccu = new JTextArea("1E-15");
        this.add(reqAccu, gc);

        gc.gridy++;
        gc.gridx = 0;
        JButton previewButton = new JButton("Preview");
        previewButton.addActionListener(e -> {
            isRunning = false;
            canvas.getGraphics().clearRect(0, 0, size, size);
            startButton.setText("Start");
            RefreshPreview();
        });
        this.add(previewButton, gc);

        gc.gridx = 1;
        startButton = new JButton("Start");
        startButton.addActionListener(e -> {
            if (Params.CheckNull())
                return;

            if (isRunning) {
                isRunning = false;
                graphics.clearRect(0, 0, size, size);
                canvas.getGraphics().clearRect(0, 0, size, size);
                startButton.setText("Start");
            } else {
                try {
                    BigDecimal ra = new BigDecimal(reqAccu.getText());
                    isRunning = true;
                    startButton.setText("Stop");
                    canvas.getGraphics().clearRect(0, 0, size, size);
                    Thread th = new Thread() {
                        public void run() {
                            if (perfLyapunov)
                                onStartedLyapunov(ra);
                            else
                                onStarted(ra);
                        }
                    };
                    th.start();
                } catch (NumberFormatException ee) {
                    JOptionPane.showMessageDialog(null, "Error: wrong number format", "Error",
                            JOptionPane.OK_OPTION);
                    isRunning = false;
                }
            }
        });
        this.add(startButton, gc);

        gc.gridy++;
        gc.gridx = 0;
        JButton RandomizeButton = new JButton("Randomize values");
        RandomizeButton.addActionListener(arg0 -> {
            Params = new SystemParams(Params.n, true);
            JOptionPane.showMessageDialog(null, "Successful", "Randomize", JOptionPane.INFORMATION_MESSAGE);
            RefreshPreview();
        });
        this.add(RandomizeButton, gc);

        gc.gridx = 1;
        JButton BalanceButton = new JButton("Balance values");
        BalanceButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (Params.CheckNull())
                    return;
                Params.balance();
                JOptionPane.showMessageDialog(null, "Successful", "Balance", JOptionPane.INFORMATION_MESSAGE);
                RefreshPreview();
            }
        });
        this.add(BalanceButton, gc);

        gc.gridy++;
        gc.gridx = 0;
        JButton LoadButton = new JButton("Load params");
        LoadButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                Load();
            }
        });
        this.add(LoadButton, gc);

        gc.gridx = 1;
        JButton SaveButton = new JButton("Save params");
        SaveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (Params.CheckNull())
                    return;
                Save();
            }
        });
        this.add(SaveButton, gc);

        gc.gridy++;

        gc.gridx = 0;
        gc.gridwidth = 1;
        JButton savePic = new JButton("Select picture path...");
        savePic.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setFileFilter(new FileNameExtensionFilter("Picture", "png"));
                if (imgFile != null)
                    fc.setCurrentDirectory(imgFile.getParentFile());
                if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                    try {
                        imgFile = fc.getSelectedFile();
                        if (!fc.getSelectedFile().toString().endsWith(".png"))
                            imgFile = new File(fc.getSelectedFile().toString() + ".png");
                        refreshImgFile();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Error saving file", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        this.add(savePic, gc);

        gc.gridx = 1;
        JButton saveLog = new JButton("Select log path...");
        saveLog.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setFileFilter(new FileNameExtensionFilter("Log file", "log"));
                if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                    try {
                        File log = fc.getSelectedFile();
                        if (!log.toString().endsWith(".log"))
                            log = new File(log.toString() + ".log");
                        logWrt = new PrintWriter(log);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Error saving file", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        this.add(saveLog, gc);

        gc.gridy++;
        gc.gridx = 0;
        gc.gridwidth = 2;
        gc.gridheight = 10;
        gc.fill = GridBagConstraints.NONE;
        canvas = new Canvas();
        canvas.setBackground(Color.BLACK);
        canvas.setSize(size, size);
        img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        graphics = img.createGraphics();
        ((Graphics2D) graphics).setBackground(Color.BLACK);
        this.add(canvas, gc);
        this.pack();
    }

    void onStarted(BigDecimal ra) {
        graphics.clearRect(0, 0, size, size);
        MathModel model = new MathModel(Params.getParams(), true);
        model.setRequiredAccu(ra);
        model.setAccu(60);
        int rStep = 10;
        while (isRunning) if (model.t % rStep == 0) {
            DrawPoints(model.Step());
            canvas.getGraphics().drawImage(img, 0, 0, null);
            if (model.t % (rStep * 100) == 0)
                refreshImgFile();
        } else
            DrawPoints(model.Step());
    }

    void refreshImgFile() {
        Thread th = new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    if (imgFile != null)
                        ImageIO.write(img, "png", imgFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        th.start();
    }

    void printStepSummary(LyapunovModel lmod)
    {
       PrintWriter pw = new PrintWriter(System.out);
       pw.println("Model accu summary:");
       pw.println(lmod.cModel.getStep());
       pw.println(lmod.cModel.getTime());
       for(MathModel mm : lmod.models){
           pw.println(mm.getStep());
           pw.println(mm.getTime());
       }
       pw.println();
       pw.flush();
    }

    void onStartedLyapunov(BigDecimal ra) {
        graphics.clearRect(0, 0, size, size);
        LyapunovModel model = new LyapunovModel(Params, ra, 60);
        int ss = 10;
        while (isRunning) {
            Vector[][] vecs = model.performStep();
            for (int i = 0; i < vecs.length; i++) {
                DrawPoints(vecs[i]);
            }
            if (model.t % ss == 0) {
                canvas.getGraphics().drawImage(img, 0, 0, null);
                if (model.t % (ss * 100) == 0){
                    refreshImgFile();
                    printStepSummary(model);
                }
                if (model.retrievePLs() == null) {
                    System.out.println("Temporary error.");
                    continue;
                }
                double[] mat = model.retrievePLs();

                double sum = 0;
                boolean print = true;
                if (logWrt != null) {
                    for (int i = 0; i < mat.length; i++) {
                        logWrt.print(mat[i] + " ");
                        sum += mat[i];
                        logWrt.println();
                    }
                    logWrt.println("Sum: " + sum);
                    logWrt.println("\n");
                }
            }
        }
    }

    void DrawPoints(Vector[] points) {
        for (int i = 0; i < points.length; i++) {
            float colorID = i / (float) (points.length);
            graphics.setColor(Color.getHSBColor(colorID, 1, 1));
            graphics.fillOval(size / 2 + points[i].x - 2, size / 2 - points[i].y - 2, 2, 2);
        }
    }

    void RefreshPreview() {
        graphics.clearRect(0, 0, size, size);
        for (int i = 0; i < Params.n; i++) {
            try {
                isRunning = false;
                float colorID = i / (float) (Params.n);
                graphics.setColor(Color.getHSBColor(colorID, 1, 1));
                Vector Position = Params.getPosition(i);
                Vector Speed = Params.getVelocity(i);

                graphics.fillOval(size / 2 + Position.x - 2, size / 2 - Position.y - 2, 5, 5);
                graphics.drawLine(size / 2 + Position.x, size / 2 - Position.y,
                        size / 2 + Position.x + (int) (Speed.X.doubleValue() * 100000),
                        size / 2 - Position.y - (int) (Speed.Y.doubleValue() * 100000));
            } catch (NullPointerException e) {
                JOptionPane.showMessageDialog(null, "Error: no info about planet #" + (i + 1), "Error",
                        JOptionPane.ERROR_MESSAGE);
                graphics.clearRect(0, 0, size, size);
            }
        }
        canvas.getGraphics().drawImage(img, 0, 0, null);
    }

    private JTextArea posx = new JTextArea("");
    private JTextArea posy = new JTextArea("");
    private JTextArea velx = new JTextArea("");
    private JTextArea vely = new JTextArea("");
    private JTextArea mass = new JTextArea("");

    private BigDecimal[] getLaunchParams(BigDecimal[] inParams, int ind) {
        JLabel lposx = new JLabel("Position X (x1000 km)");
        JLabel lposy = new JLabel("Position Y (x1000 km)");
        JLabel lvelx = new JLabel("Velocity X (km/s)");
        JLabel lvely = new JLabel("Velocity Y (km/s)");
        JLabel lmass = new JLabel("Mass (x10^22 kg)");

        posx = new JTextArea("");
        posy = new JTextArea("");
        velx = new JTextArea("");
        vely = new JTextArea("");
        mass = new JTextArea("");

        JButton rand = new JButton("Randomize values");
        rand.addActionListener(e -> {
            BigDecimal[] loc = SystemParams.generatePlanet();
            posx.setText(loc[0].toString());
            posy.setText(loc[1].toString());
            velx.setText(loc[2].multiply(new BigDecimal(1000)).toString());
            vely.setText(loc[3].multiply(new BigDecimal(1000)).toString());
            mass.setText(loc[4].toString());
        });

        if (inParams != null) {
            posx = new JTextArea(inParams[0].toString());
            posy = new JTextArea(inParams[1].toString());
            velx = new JTextArea(inParams[2].multiply(new BigDecimal(1000)).toString());
            vely = new JTextArea(inParams[3].multiply(new BigDecimal(1000)).toString());
            mass = new JTextArea(inParams[4].toString());
        }

        final JComponent[] inputs = new JComponent[]{lposx, posx, lposy, posy, lvelx, velx, lvely, vely, lmass, mass,
                rand};
        boolean flagg = true;
        BigDecimal[] lParams = new BigDecimal[5];
        do {
            int result = JOptionPane.showConfirmDialog(null, inputs, "Launch parameters of #" + ind,
                    JOptionPane.DEFAULT_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    lParams[0] = new BigDecimal(posx.getText());
                    lParams[1] = new BigDecimal(posy.getText());
                    lParams[2] = new BigDecimal(velx.getText()).multiply(new BigDecimal("0.001"));
                    lParams[3] = new BigDecimal(vely.getText()).multiply(new BigDecimal("0.001"));
                    lParams[4] = new BigDecimal(mass.getText());
                    flagg = false;
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Wrong number format", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else if (result == JOptionPane.CLOSED_OPTION) {
                return inParams;
            } else {
                flagg = false;
            }
        } while (flagg);

        return lParams;
    }

    void Load() {
        JFileChooser fc = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Config files", "config");
        fc.setFileFilter(filter);
        int returnVal = fc.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                Scanner sc = new Scanner(fc.getSelectedFile());
                String s = "";
                while (sc.hasNextLine())
                    s += sc.nextLine() + '\n';
                sc.close();
                Params = new SystemParams(s);
                com.removeAllItems();
                for (int i = 0; i < Params.n; i++)
                    com.addItem(i + 1);
                RefreshPreview();
            } catch (FileNotFoundException e) {

                JOptionPane.showMessageDialog(null, "Error loading file", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    void Save() {
        JFileChooser fc = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Config files", "config");
        fc.setFileFilter(filter);
        int returnVal = fc.showSaveDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                String fcc = fc.getSelectedFile().toString();
                if (!fcc.endsWith(".config"))
                    fcc += ".config";
                PrintWriter pw = new PrintWriter(new File(fcc));
                pw.println(Params);
                pw.flush();
            } catch (FileNotFoundException e) {

                JOptionPane.showMessageDialog(null, "Error saving file", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}