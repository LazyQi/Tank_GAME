/*功能：坦克大战
* 1.画出坦克
* 2.我的坦克可以上下左右移动
* 3.我的坦克可以发射子弹，子弹连发，最多5颗
* 4.当我的坦克击中敌人坦克时，敌人坦克消失（爆炸效果）
* 5.我被击中后，显示爆炸效果
* 6.防止敌人坦克重叠运动
* 7.可以分关
*
* 8.游戏可以暂停和继续(当用户点击暂停时，子弹和坦克的速度设为0并且坦克的方向不要变化)
* 9.可以记录玩家的成绩（1.用文件流的方式【小游戏】2.单写一个记录类，完成对游戏的记录 3.先完成保存共击毁了多少辆敌人坦克的功能
*   4.存盘退出游戏）
* 10.java操作声音文件
* （1.省略好了。。）
* */


import com.sun.org.apache.bcel.internal.generic.ALOAD;
import com.sun.org.apache.bcel.internal.generic.BREAKPOINT;
import com.sun.org.apache.regexp.internal.RE;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import com.sun.xml.internal.bind.v2.TODO;
import org.omg.PortableServer.THREAD_POLICY_ID;
import sun.awt.image.ToolkitImage;
import sun.security.krb5.internal.crypto.EType;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import java.io.*;
import java.nio.channels.OverlappingFileLockException;
import java.util.TreeMap;
import java.util.Vector;
public class Simple_tank_game extends JFrame implements ActionListener{
    //定义一个开始面板
    MyStartPanel myStartPanel = null;
    //作出我需要的菜单
    JMenuBar jmb = null;
    //开始游戏 键
    JMenu jm1=null;
    JMenuItem jmi1 = null;
    //退出系统 键
    JMenuItem jmi2 = null;
    //存盘退出 键
    JMenuItem jmi3 = null;
    //去上局 键
    JMenuItem jmi4 = null;

    //定义一个游戏面板
    myPanel mp = null;
    public Simple_tank_game(){
        //创建菜单及菜单选项
        jmb = new JMenuBar();
        jm1 = new JMenu("游戏(G)");
        //设置快捷方式
        jm1.setMnemonic('G');
        jmi1 = new JMenuItem("开始新游戏（N）");
        jmi1.setMnemonic('N');
        jmi1.addActionListener(this);
        jmi1.setActionCommand("new game");

        jmi2=new JMenuItem("退出游戏（E）");
        jmi2.setMnemonic('E');
        //注册监听
        jmi2.addActionListener(this);
        jmi2.setActionCommand("exit game");

        //存盘退出游戏
        jmi3 =new JMenuItem("存盘退出（C）");
        jmi3.addActionListener(this);
        jmi3.setActionCommand("save exit");

        //回到上局游戏
        jmi4 = new JMenuItem("继续上局游戏（S）");
        jmi4.addActionListener(this);
        jmi4.setActionCommand("continue");

        jm1.add(jmi1);
        jm1.add(jmi2);
        jm1.add(jmi3);
        jm1.add(jmi4);
        jmb.add(jm1);

        myStartPanel = new MyStartPanel();
        Thread t = new Thread(myStartPanel);
        t.start();

        this.setJMenuBar(jmb );
        this.add(myStartPanel);
        this.setSize(550,450);
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    public static void main(String[] args) {
        Simple_tank_game game = new Simple_tank_game();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("new game")){
            //在这里创造战场面板
            mp = new myPanel("new game");
            //启动mp线程
            Thread t = new Thread(mp);
            t.start();
            //******先删除旧面板
            this.remove(myStartPanel);
            this.add(mp);
            //注册监听
            this.addKeyListener(mp);
            //显示出新面板，刷新JFrame
            this.setVisible(true);
        }
        else if (e.getActionCommand().equals("exit game")){
            //用户点击了退出游戏
            //保存击毁敌人数量
            Recorder.keepRecording();
            //退出系统,0代表正常退出，1代表异常退出
            System.exit(0);
        }
        else if (e.getActionCommand().equals("save exit")){
            //TODO：1.保存击毁敌人的数量和坐标
            Recorder.setEts(mp.enemyTanks);
            Recorder.keepEnemyTankInfo();
            System.exit(0);
        }
        else if(e.getActionCommand().equals("continue")){
            //创造战场面板
            mp = new myPanel("continue");

            //启动mp线程
            Thread t = new Thread(mp);
            t.start();
            //******先删除旧面板
            this.remove(myStartPanel);
            this.add(mp);
            //注册监听
            this.addKeyListener(mp);
            //显示出新面板，刷新JFrame
            this.setVisible(true);
        }

    }
}

class MyStartPanel extends JPanel implements Runnable{
    int times=0;
    public void paint(Graphics g){
        super.paint(g);

        g.fillRect(0,0,400,300);

        if (times%2==0){
            g.setColor(Color.yellow);
            Font myFont = new Font("华文新魏",Font.BOLD,30);
            g.setFont(myFont);
            //提示信息
            g.drawString("Round: 1",150,150);}
    }

    @Override
    public void run() {
        //闪烁显示关卡面板
        while (true){
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            times++;
            this.repaint();
        }
    }
}

class myPanel extends JPanel implements KeyListener, Runnable{
    //定义一个我方坦克
    MyTank hero = null;

    //定义一个敌人坦克集合，线程安全用Vector
    Vector<EnemyTank> enemyTanks = new Vector<>();
    //上局游戏剩余的坦克
    Vector<Node> nodes = new Vector<>();
    static int enemyTanksize = 4;
    int x =300;
    int y =100;
    int speed = 5;

    //定义炸弹集合
    Vector<bomb> bombs = new Vector<>();

    //爆炸效果图片,三张图片组成一颗炸弹
    Image image1=null;
    Image image2=null;
    Image image3=null;

    //构造函数初始化
    public myPanel(String flag){
        //恢复游戏记录
        Recorder.getRecording();
        //我方坦克
        hero = new MyTank(x,y,speed);
        if (flag.equals("new game")){
            //敌方坦克
            for(int i=0; i<enemyTanksize;i++){
                //创建敌人坦克对象
                EnemyTank et = new EnemyTank((i+1)*50, 0,2);
                et.setColor(0);
                et.setDirection(2);
                //将MyPanel的敌人坦克向量交给该敌人坦克
                et.setEts(enemyTanks);
                //启动敌人的坦克
                Thread t = new Thread(et);
                t.start();
                //给敌人坦克添加子弹
                shot s = new shot(et.x+10, et.y+30,2);
                et.ss.add(s);
                Thread t2 = new Thread(s);
                t2.start();
                enemyTanks.add(et);
            }
        }
        else {
            nodes = new Recorder().getNodes();
            for (int i = 0; i < nodes.size(); i++) {
                //取出上局游戏的EnemyTank
                Node node = nodes.get(i);
                //创建敌人坦克对象
                EnemyTank et = new EnemyTank(node.x, node.y, 2);
                et.setColor(0);
                et.setDirection(node.direction);
                //将MyPanel的敌人坦克向量交给该敌人坦克
                et.setEts(enemyTanks);
                //启动敌人的坦克
                Thread t = new Thread(et);
                t.start();
                //给敌人坦克添加子弹
                shot s = new shot(et.x + 10, et.y + 30, 2);
                et.ss.add(s);
                Thread t2 = new Thread(s);
                t2.start();
                enemyTanks.add(et);
            }
        }
//        try {
//            image1 = ImageIO.read(new File("/Tank_Game/bomb1.png"));
//            image2 = ImageIO.read(new File("/Tank_Game/bomb3.png"));
//            image3 = ImageIO.read(new File("/Tank_Game/bomb2.png"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        //初始化爆炸图片
        image1= Toolkit.getDefaultToolkit().getImage(Panel.class.getResource("/bomb1.png"));
        image2= Toolkit.getDefaultToolkit().getImage(Panel.class.getResource("/bomb3.png"));
        image3= Toolkit.getDefaultToolkit().getImage(Panel.class.getResource("/bomb2.png"));

    }

    public void showInfo(Graphics g){

        //画出显示提示信息的坦克，该坦克不参与战斗
        this.draw_a_tank(80,330,g,0,0);
        g.setColor(Color.black);
        g.drawString(Recorder.getEnNum()+"",110,350);

        this.draw_a_tank(150,330,g,0,1);
        g.setColor(Color.black);
        g.drawString(Recorder.getMyLife()+"",180,350);
        //画出玩家的总成绩
        g.setColor(Color.black);
        Font F=new Font("宋体",Font.BOLD,20);
        g.setFont(F);
        g.drawString("您的总成绩",420,30);
        this.draw_a_tank(430,55,g,0,0);

        g.setColor(Color.black);
        g.drawString(Recorder.getSlayNumOfEn()+"",460,80);
    }

    //重写paint函数
    public void paint(Graphics g){
        super.paint(g);
        //画出我的坦克
        g.fillRect(0, 0, 400, 300);
        this.showInfo(g);
        if (hero.isAlive) {
            this.draw_a_tank(this.hero.x, this.hero.y, g, this.hero.direction, 1);
        }

        //画出子弹
        //从ss中取出子弹。
        for (int i = 0; i <hero.ss.size() ; i++) {
            shot myshot = hero.ss.get(i);
            //画出一颗子弹
            if (myshot!=null&&myshot.isAlive==true){
                g.draw3DRect(myshot.x,myshot.y,1,1,false);
            }
            if (myshot.isAlive==false){
                hero.ss.remove(myshot);
            }
        }
        //画出炸弹的爆炸效果
        for (int i = 0; i <bombs.size() ; i++) {
            bomb b = bombs.get(i);
            if (b.life>6){
                g.drawImage(image3,b.x,b.y,30,30,this);
            }
            else if (b.life>3){
                g.drawImage(image2,b.x,b.y,30,30,this);
            }
            else {
                g.drawImage(image1,b.x,b.y,30,30,this);
            }
            //让b的生命值减小
            b.lifeDown();
            if (b.life==0){
                bombs.remove(b);
            }
        }

        //画出敌人的坦克
        for(int i=0; i<enemyTanks.size(); i++){
            EnemyTank et = enemyTanks.get(i);
            if (et.isAlive){
                this.draw_a_tank(et.getX(), et.getY(),g,et.getDirection(),0);}
            //and paint the bullet
            for (int j = 0; j < et.ss.size(); j++) {
                //取出子弹
                shot es = et.ss.get(j);
                if (es.isAlive){
                    g.draw3DRect(es.x,es.y,1,1,false);
                }
                else {
                    //如果敌人坦克死亡
                    et.ss.remove(es);
                }
            }
        }
    }

    //写一个函数判断子弹是否击中敌人坦克
    public boolean hitTank(shot s, Tank Tank){
        boolean isHit = false;
        //判断坦克方向
        switch(Tank.direction){
            //如果敌人坦克向上或者向下
            case 0:
            case 2:
                if (s.x>Tank.x&&s.x<Tank.x+20&&s.y>Tank.y&&s.y<Tank.y+30){
                    s.isAlive=false;
                    Tank.isAlive = false;
                    isHit =true;
//                    //减少敌人数量
//                    Recorder.ReduceEne();
//                    //增加玩家成绩
//                    Recorder.AddSlayEn();
                    //创建一颗炸弹,放入vector
                    bomb b = new bomb(Tank.x,Tank.y);
                    bombs.add(b);
                }
                break;
            case 1:
            case 3:
                if (s.x>Tank.x&&s.x<Tank.x+30&&s.y>Tank.y&&s.y< Tank.y+20){
                    s.isAlive=false;
                    Tank.isAlive = false;
                    isHit=true;
//                    Recorder.ReduceEne();
//                    Recorder.AddSlayEn();
                    //创建一颗炸弹,放入vector
                    bomb b = new bomb(Tank.x,Tank.y);
                    bombs.add(b);
                }
                break;
        }
        return isHit;
    }

    //判断我的子弹是否击中敌人坦克
    public void isHitEnemy(){
        for (int i = 0; i < hero.ss.size(); i++) {
            //取出子弹
            shot myShot = hero.ss.get(i);
            if (myShot.isAlive){
                //取出每一个敌人坦克与该子弹判断
                for (int j = 0; j <enemyTanks.size() ; j++) {
                    EnemyTank et = enemyTanks.get(j);
                    if (et.isAlive){
                        if (this.hitTank(myShot, et)){
                            Recorder.ReduceEne();
                            Recorder.AddSlayEn();
                        }
                    }
                }
            }
        }

    }

    public void isHitMe(){
        //取出每一个敌人坦克的每一颗子弹
        for (int i = 0; i < this.enemyTanks.size(); i++) {
            EnemyTank et = enemyTanks.get(i);
            for (int j = 0; j <et.ss.size(); j++) {
                shot es = et.ss.get(j);
                if (hero.isAlive)
                {
                    if(this.hitTank(es, hero));
                    {
                        //TODO
                    }
                }
            }
        }
    }

    //画出坦克的函数
    public void draw_a_tank(int x, int y, Graphics g, int direction, int type){
        switch (type){
            //画我方坦克
            case 0:
                g.setColor(Color.red);
                break;
            case 1:
                g.setColor(Color.green);
                break;
        }
        switch (direction){
            case 0://坦克炮筒向上
                g.fill3DRect(x, y,5,30, false);
                g.fill3DRect(x+15, y,5,30, false);
                g.fill3DRect(x+5, y+5,10,20, false);
                g.drawOval(x+4,y+10,10,10);
                g.drawLine(x+9,y+10,x+9,y);
                break;
            case 1://炮筒向右
                g.fill3DRect(x, y,30,5, false);
                g.fill3DRect(x, y+15,30,5, false);
                g.fill3DRect(x+5, y+5,20,10, false);
                g.drawOval(x+10,y+5,10,10);
                g.drawLine(x+15,y+10,x+30,y+10);
                break;
            case 2://炮筒向下
                g.fill3DRect(x, y,5,30, false);
                g.fill3DRect(x+15, y,5,30, false);
                g.fill3DRect(x+5, y+5,10,20, false);
                g.drawOval(x+4,y+10,10,10);
                g.drawLine(x+9,y+10,x+9,y+30);
                break;
            case 3://炮筒向右
                g.fill3DRect(x, y,30,5, false);
                g.fill3DRect(x, y+15,30,5, false);
                g.fill3DRect(x+5, y+5,20,10, false);
                g.drawOval(x+10,y+5,10,10);
                g.drawLine(x+15,y+10,x,y+10);
                break;

        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode()==KeyEvent.VK_W){
            this.hero.setDirection(0);
            this.hero.moveUp();
        }
        else if(e.getKeyCode()==KeyEvent.VK_A){
            this.hero.setDirection(3);
            this.hero.moveLeft();
        }
        else if(e.getKeyCode()==KeyEvent.VK_S){
            this.hero.setDirection(2);
            this.hero.moveDown();
        }
        else if(e.getKeyCode()==KeyEvent.VK_D){
            this.hero.setDirection(1);
            this.hero.moveRight();
        }
        if (e.getKeyCode()==KeyEvent.VK_J){
            //判断玩家是否需要开火
            if (hero.ss.size()<=4){
                this.hero.shotEnemy();}
        }
        this.repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void run() {
        //每隔100ms去重绘
        while(true) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //在run的过程中要一直判断子弹是否击中坦克
            this.isHitEnemy();
            //判断敌人的坦克是否击中我方坦克
            this.isHitMe();
            this.repaint();
        }
    }
}

class bomb{
    int x,y;
    //炸弹的生命
    int life = 9;
    boolean isAlive = true;
    public bomb(int x, int y){
        this.x=x;
        this.y=y;
    }
    public void lifeDown(){
        if (life>0){
            life--;
        }
        else {
            this.isAlive = false;
        }
    }
}

class shot implements Runnable{
    int x;
    int y;
    int direction;
    int speed=5;
    //是否还活着
    boolean isAlive = true;
    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(50);
            } catch (Exception e){}
            switch (direction) {
                case 0:
                    //子弹向上
                    y -= speed;
                    break;
                case 1:
                    x += speed;
                    break;
                case 2:
                    y += speed;
                    break;
                case 3:
                    x -= speed;
                    break;
            }
//            System.out.println("子弹坐标x=" + x + "y=" + y);
            //判断该子弹是否碰到边缘
            if (x<0||x>400||y<0||y>300){
                this.isAlive = false;
                break;
            }
        }
    }

    public shot(int x, int y, int direction){
        this.x=x;
        this.y=y;
        this.direction = direction;
    }
}

class Tank{
    int x = 0;
    int y = 0;
    // 坦克方向
    // 0表示向上，1 右，2下，3左
    int direction = 0;
    //坦克颜色
    int color;
    int speed = 1;
    boolean isAlive=true;
    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Tank(int x, int y, int speed){
        this.x = x;
        this.y = y;
        this.speed = speed;
    }
}

class EnemyTank extends Tank implements Runnable{
    //    boolean isAlive = true;
    int times=0;

    //定义一个向量，可以访问MyPanel上所有敌人的坦克
    Vector<EnemyTank> ets = new Vector<>();

    //vector 存放敌人的子弹
    Vector<shot> ss = new Vector<>();
    //敌人添加子弹，应该在刚刚创建敌人坦克，和敌人坦克的子弹死亡后添加

    public EnemyTank(int x,int y, int speed){
        super(x,y,speed);
    }

    @Override
    public void run() {
        while (true) {
            switch (this.direction){
                case 0:
                    //tank is up
                    for (int i = 0; i < 30 ; i++) {
                        if (y>0&&!this.isTouchOtherEnemy()){
                            y-=speed;}
                        try {
                            Thread.sleep(50);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case 1:
                    for (int i = 0; i < 30 ; i++) {
                        if (x<370&&!this.isTouchOtherEnemy()){
                            x+=speed;
                        }
                        try {
                            Thread.sleep(50);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case 2:
                    for (int i = 0; i < 30 ; i++) {
                        if (y<270&&!this.isTouchOtherEnemy()){
                            y+=speed;
                        }
                        try {
                            Thread.sleep(50);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    break;
                case 3:
                    for (int i = 0; i < 30 ; i++) {
                        if (x>0&&!this.isTouchOtherEnemy()){
                            x-=speed;
                        }
                        try {
                            Thread.sleep(50);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
            this.times++;
            if (times%2==0)
            {
                //判断是否需要给坦克加入新的子弹
                if (isAlive){
                    if (ss.size()<5){
                        //没有子弹了，添加子弹
                        shot s = null;
                        switch (direction){
                            case 0:
                                //创建一颗子弹
                                s = new shot(x+10,y,0);
                                ss.add(s);
                                break;
                            case 1:
                                s = new shot(x+30,y+10,1);
                                ss.add(s);
                                break;
                            case 2:
                                s = new shot(x+10,y+30,2);
                                ss.add(s);
                                break;
                            case 3:
                                s = new shot(x,y+10,3);
                                ss.add(s);
                                break;
                        }
                        //启动子弹
                        Thread t = new Thread(s);
                        t.start();
                    }
                }

            }
            //让坦克随机产生一个新的方向
            this.direction=(int)(Math.random()*4);
            //判断敌人坦克是否死亡
            if (this.isAlive==false){
                //让坦克死亡后退出线程
                break;
            }
        }
    }

    //得到MyPanel的敌人坦克向量
    public void setEts(Vector<EnemyTank> vv){
        this.ets=vv;
    }

    //判断是否碰到别的敌人坦克
    public boolean isTouchOtherEnemy(){
        boolean res = false;
        switch (this.direction){
            case 0:
                //我的坦克向上
                //取出所有的敌人坦克
                for (int i = 0; i <ets.size() ; i++) {
                    EnemyTank et=ets.get(i);
                    //如果不是自己
                    if (et!=this){
                        //另一敌人的方向向上或向下
                        if (et.direction==0||et.direction==2){
                            if (this.x>=et.x&&this.x<=et.x+20&&this.y>=et.y&&this.y<=et.y+30){
                                return true;
                            }
                            if (this.x+20>=et.x&&this.x+20<=et.x+20&&this.y>=et.y&&this.y<=et.y+30){
                                return true;
                            }
                        }
                        if (et.direction==3||et.direction==1){
                            if (this.x>=et.x&&this.x<=et.x+30&&this.y>=et.y&&this.y<=et.y+30){
                                return true;
                            }
                            if (this.x+20>=et.x&&this.x+20<=et.x+30&&this.y>=et.y&&this.y<=et.y+20){
                                return true;
                            }
                        }
                    }
                }
                break;
            case 1:
                //坦克向右
                for (int i = 0; i <ets.size() ; i++) {
                    EnemyTank et=ets.get(i);
                    //如果不是自己
                    if (et!=this){
                        if (et.direction==0||et.direction==2){
                            if (this.x+30>=et.x&&this.x + 30<=et.x+20&&this.y>=et.y&&this.y<=et.y+30){
                                return true;
                            }
                            if (this.x+30>=et.x&&this.x+30<=et.x+20&&this.y+20>=et.y&&this.y+20<=et.y+30){
                                return true;
                            }
                        }
                        if (et.direction==3||et.direction==1){
                            if (this.x+30>=et.x&&this.x+30<=et.x+30&&this.y>=et.y&&this.y<=et.y+30){
                                return true;
                            }
                            if (this.x+30>=et.x&&this.x+30<=et.x+30&&this.y+20>=et.y&&this.y+20<=et.y+20){
                                return true;
                            }
                        }
                    }
                }
                break;
            case 2:
                //此坦克向下
                for (int i = 0; i <ets.size() ; i++) {
                    EnemyTank et=ets.get(i);
                    //如果不是自己
                    if (et!=this){
                        //另一敌人的方向向上或向下
                        if (et.direction==0||et.direction==2){
                            //我的左点
                            if (this.x>=et.x&&this.x<=et.x+20&&this.y+30>=et.y&&this.y+30<=et.y+30){
                                return true;
                            }
                            //我的右点
                            if (this.x+20>=et.x&&this.x<=et.x+20&&this.y+30>=et.y&&this.y+30<=et.y+30){
                                return true;
                            }
                        }
                        if (et.direction==3||et.direction==1){
                            if (this.x>=et.x&&this.x<=et.x+30&&this.y+30>=et.y&&this.y+30<=et.y+30){
                                return true;
                            }
                            if (this.x+20>=et.x&&this.x+20<=et.x+30&&this.y+30>=et.y&&this.y+30<=et.y+20){
                                return true;
                            }
                        }
                    }
                }
                break;
            case 3:
                //此坦克向左
                for (int i = 0; i <ets.size() ; i++) {
                    EnemyTank et=ets.get(i);
                    //如果不是自己
                    if (et!=this){
                        //另一敌人的方向向上或向下
                        if (et.direction==0||et.direction==2){
                            if (this.x>=et.x&&this.x<=et.x+20&&this.y>=et.y&&this.y<=et.y+30){
                                return true;
                            }
                            if (this.x>=et.x&&this.x<=et.x+20&&this.y+20>=et.y&&this.y+20<=et.y+30){
                                return true;
                            }
                        }
                        if (et.direction==3||et.direction==1){
                            if (this.x>=et.x&&this.x<=et.x+30&&this.y>=et.y&&this.y<=et.y+30){
                                return true;
                            }
                            if (this.x>=et.x&&this.x<=et.x+30&&this.y+20>=et.y&&this.y+20<=et.y+20){
                                return true;
                            }
                        }
                    }
                }
                break;
        }
        return res;

    }
}

class MyTank extends Tank{
    //子弹看作坦克的一种属性
    Vector<shot> ss = new Vector<>();
    shot s = null;

    public MyTank(int x, int y, int speed){
        super(x, y, speed);
    }

    public void shotEnemy(){
        switch (this.direction){
            case 0:
                //创建一颗子弹
                s = new shot(x+10,y,0);
                ss.add(s);
                break;
            case 1:
                s = new shot(x+30,y+10,1);
                ss.add(s);
                break;
            case 2:
                s = new shot(x+10,y+30,2);
                ss.add(s);
                break;
            case 3:
                s = new shot(x,y+10,3);
                ss.add(s);
                break;
        }
        //启动子弹线程
        Thread t=new Thread(s);
        t.start();

    }
    public void moveUp(){
        if (y>0)
            y-=speed;
    }
    public void moveDown(){
        if (y<270)  y+=speed;
    }
    public void moveRight(){
        if (x<370)
            x+=speed;
    }
    public void moveLeft(){
        if (x>0)
            x-=speed;
    }
}

class Recorder{
    //记录每关有多少敌人
    private static int enNum = myPanel.enemyTanksize;//default
    //设置我的生命次数
    private static int myLife = 3;
    //记录消灭敌人数
    private static int SlayNumOfEn=0;
    //从文件中恢复记录点
    static Vector<Node> nodes = new Vector<>();
    //字符流
    private static FileWriter fw = null;
    private static FileReader fr = null;
    //提高效率
    private static BufferedWriter bw = null;
    private static BufferedReader br = null;

    public static Vector<EnemyTank> getEts() {
        return ets;
    }

    public static void setEts(Vector<EnemyTank> ets) {
        Recorder.ets = ets;
    }

    private static Vector<EnemyTank> ets = new Vector<>();

    //把玩家击毁的坦克数量保存到文件中
    public static void keepRecording(){
        //创建文件流
        try {
            fw = new FileWriter("F:/TankGame_Record/myRecording.txt");
            bw = new BufferedWriter(fw);
            bw.write(SlayNumOfEn+"\r\n");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //关闭文件流，原则是，后开先关
            try {
                bw.close();
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    //保存击毁敌人的数量和敌人坦克坐标，方向
    public static void keepEnemyTankInfo(){
        //创建文件流
        try {
            fw = new FileWriter("F:/TankGame_Record/myRecording.txt");
            bw = new BufferedWriter(fw);
            bw.write(SlayNumOfEn+"\r\n");

            //保存当前活的敌人坐标，方向
            for (int i = 0; i <ets.size() ; i++) {
                EnemyTank et = ets.get(i);
                if (et.isAlive){
                    //活的才保存
                    String info = et.x+" "+et.y+" "+et.direction;
                    bw.write(info+"\r\n");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //关闭文件流，原则是，后开先关
            try {
                bw.close();
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static Vector<Node> getNodes(){
        try {
            fr = new FileReader("F:/TankGame_Record/myRecording.txt");
            br = new BufferedReader(fr);
            String n = "";
            try {
                //先读第一行
                n = br.readLine();
                while((n=br.readLine())!=null){
                    String[] xyd = n.split(" ");
                    Node node = new Node(Integer.parseInt(xyd[0]),Integer.parseInt(xyd[1]),Integer.parseInt(xyd[2]));
                    nodes.add(node);

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            //后开先关
            try {
                br.close();
                fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return nodes;
    }

    //从文件中读取记录
    public static void  getRecording(){
        try {
            fr = new FileReader("F:/TankGame_Record/myRecording.txt");
            br = new BufferedReader(fr);
            String n = "";
            try {
                n = br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            SlayNumOfEn = Integer.parseInt(n);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            //后开先关
            try {
                br.close();
                fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static int getSlayNumOfEn() {
        return SlayNumOfEn;
    }

    public static  void setSlayNumOfEn(int slayNumOfEn) {
        SlayNumOfEn = slayNumOfEn;
    }

    public static  int  getEnNum() {
        return enNum;
    }
    public static void setEnNum(int enNum) {
        Recorder.enNum = enNum;
    }
    public static int  getMyLife() {
        return myLife;
    }
    public static void setMyLife(int myLife) {
        Recorder.myLife = myLife;
    }

    //减少敌人数量(直接通过类就可以访问静态方法)
    public static void ReduceEne(){
        enNum--;
    }
    public static void ReduceMylife(){
        myLife--;
    }
    public static void AddSlayEn(){
        SlayNumOfEn++;
    }

}

class Node{
    int x;
    int y;
    int direction;
    public Node(int x,int y, int direction){
        this.x = x;
        this.y = y;
        this.direction = direction;
    }
}