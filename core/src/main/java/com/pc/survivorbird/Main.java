package com.pc.survivorbird;

// Gerekli LibGDX ve Java kütüphaneleri

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.graphics.Color;


import java.util.Random;


public class Main extends ApplicationAdapter {
    // Grafiklerin çizimi için SpriteBatch nesnesi tanımlanır.
    SpriteBatch batch;

    // Oyunun görselleri için Texture nesneleri tanımlanır.
    Texture background;
    Texture bird;
    Texture bee1;
    Texture bee2;
    Texture bee3;

    float birdX = 0; // Kuşun X eksenindeki başlangıç konumu.
    float birdY = 0; // Kuşun Y eksenindeki başlangıç konumu.
    int gameState = 0; // Oyunun mevcut durumu (0: Başlangıç, 1: Oyun devam ediyor, 2: Oyun bitti)
    float velocity = 0; // Kuşun düşüş hızı.
    float gravity = 1; // Yerçekimi etkisi.
    float enemyVelocity = 7; // Engellerin hareket hızı.

    Random random; // Rastgele arı konumları için rastgele sayı üretici.
    int score = 0; // Oyuncunun mevcut puanı.
    int scoreEnemy = 0; // Puanın hangi arı ilişkili olduğunu takip eder.

    BitmapFont font; // Puan göstergesi için font.
    BitmapFont font2; // Oyun bittiğinde gösterilen mesaj için font.

    // Kuş ve engeller arasındaki çarpışmaları algılamak için çember nesneleri.
    Circle birdCircle;

    // Şekil çizer (Çarpışma kutuları görünür yapılabilir).
    ShapeRenderer shapeRenderer;

    // Arıların konum ve hareketleri için diziler.
    int numberOfEnemies = 4; // Oyunda kaç tane düşman olacağı.
    float [] enemyX = new float [numberOfEnemies]; // Arıların X konumları.
    float [] enemyOfSet = new float [numberOfEnemies]; // Arıların Y konum ofsetleri.
    float [] enemyOfSet2 = new float [numberOfEnemies];
    float [] enemyOfSet3 = new float [numberOfEnemies];
    float distance = 0; // Arılar arasındaki mesafe.

    Circle[] enemyCircles;
    Circle[] enemyCircles2;
    Circle[] enemyCircles3;


    @Override
    // Oyun başladığında çalışacak olan metot.
    public void create() {

        // Grafiklerin çizilmesi için SpriteBatch nesnesi başlatılır.
        batch = new SpriteBatch();

        // Görseller yüklenir.
        background = new Texture("mountain.png");
        bird= new Texture("bird.png");
        bee1 = new Texture("bee.png");
        bee2 = new Texture("bee.png");
        bee3 = new Texture("bee.png");

        // Arılar arasındaki mesafe belirlenir.
        distance = Gdx.graphics.getWidth()/2;

        // Rastgele sayı üretici başlatılır.
        random = new Random();

        // Kuşun başlangıç konumu ayarlanır.
        birdX = Gdx.graphics.getWidth()/2 - bird.getHeight()/2;
        birdY = Gdx.graphics.getHeight()/3;

        // Şekil çizer başlatılır.
        shapeRenderer = new ShapeRenderer();

        // Çarpışma kontrolü için çember nesneleri oluşturulur.
        birdCircle = new Circle();
        enemyCircles = new Circle[numberOfEnemies];
        enemyCircles2 = new Circle[numberOfEnemies];
        enemyCircles3 = new Circle[numberOfEnemies];

        // Yazı fontları ayarlanır.
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(4);

        font2 = new BitmapFont();
        font2.setColor(Color.WHITE);
        font2.getData().setScale(7);

        // Arıların başlangıç konumları belirlenir.
        for (int i = 0; i<numberOfEnemies; i++){

            // enemyOfSet dizileri, arıların Y ekseninde rastgele konumlandırılması için kullanılır.
            // Rastgele bir değer üretilir ve ekrana uygun olacak şekilde hizalanır.
            enemyOfSet[i] = (random.nextFloat() - 0.5f) * (Gdx.graphics.getHeight() - 200);
            enemyOfSet2[i] = (random.nextFloat() - 0.5f) * (Gdx.graphics.getHeight() - 200);
            enemyOfSet3[i] = (random.nextFloat() - 0.5f) * (Gdx.graphics.getHeight() - 200);


            // enemyX dizisi, arıların X ekseninde başlangıç konumlarını belirler.
            // Arılar ekrandan sağdan girerek sola doğru hareket eder.
            enemyX[i] = Gdx.graphics.getWidth() - bee1.getWidth()/2 + i*distance;

            // Her arı için çarpışma algılaması yapılacak bir Circle nesnesi oluşturulur.
            enemyCircles[i] = new Circle();
            enemyCircles2[i] = new Circle();
            enemyCircles3[i] = new Circle();
        }

    }

    @Override
    public void render() {

        // Grafiklerin çizilmeye başlanacağını belirtir.
        batch.begin();
        // Arkaplan resmi ekrana çizilir.
        batch.draw(background,0,0,Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Oyun durumu kontrol edilir: 1 → Oyun devam ediyor
        if(gameState == 1){

            // Skor artışı kontrolü: Kuş, arıların hizasından geçtiyse skor artırılır.
            if(enemyX[scoreEnemy] < Gdx.graphics.getWidth()/2 - bird.getHeight()/2){
                score++;

                // Skor hesaplanan arı indeksi artırılır.
                if (scoreEnemy < numberOfEnemies - 1){
                    scoreEnemy++;
                }else{
                    scoreEnemy = 0; // Tüm arılar kontrol edildiyse başa dönülür.
                }
            }

            // Oyuncu ekrana dokunursa kuş yukarı doğru hareket eder.
            if(Gdx.input.justTouched()){
                velocity = -20; // Kuş yukarı doğru ivme kazanır.
            }

            // Tüm arılar için bir döngü başlatılır.
            for(int i = 0; i<numberOfEnemies; i++) {

                // Arı ekranın sol kenarından çıktıysa sağa alınır ve konumu sıfırlanır.
                if(enemyX[i] < -Gdx.graphics.getWidth()/15){
                    enemyX[i] = enemyX[i] + numberOfEnemies * distance;

                    // Y konumları yeniden rastgele ayarlanır.
                    enemyOfSet[i] = (random.nextFloat() - 0.5f) * (Gdx.graphics.getHeight() - 200);
                    enemyOfSet2[i] = (random.nextFloat() - 0.5f) * (Gdx.graphics.getHeight() - 200);
                    enemyOfSet3[i] = (random.nextFloat() - 0.5f) * (Gdx.graphics.getHeight() - 200);
                }else{
                    // Arı sola doğru hareket eder.
                    enemyX[i] = enemyX[i] - enemyVelocity;
                }


                // Arı görselleri çizilir.
                batch.draw(bee1, enemyX[i], Gdx.graphics.getHeight()/2 + enemyOfSet[i], Gdx.graphics.getWidth()/15, Gdx.graphics.getHeight()/10 );
                batch.draw(bee2, enemyX[i], Gdx.graphics.getHeight()/2 + enemyOfSet2[i], Gdx.graphics.getWidth()/15, Gdx.graphics.getHeight()/10 );
                batch.draw(bee3, enemyX[i], Gdx.graphics.getHeight()/2 + enemyOfSet3[i], Gdx.graphics.getWidth()/15, Gdx.graphics.getHeight()/10 );

                // Arı çemberleri güncellenir (Çarpışma kontrolleri için).
                enemyCircles[i] = new Circle(enemyX[i]+ Gdx.graphics.getWidth()/30,  Gdx.graphics.getHeight()/2 + enemyOfSet[i] + Gdx.graphics.getHeight()/20, Gdx.graphics.getWidth()/30);
                enemyCircles2[i] = new Circle(enemyX[i]+ Gdx.graphics.getWidth()/30,  Gdx.graphics.getHeight()/2 + enemyOfSet2[i] + Gdx.graphics.getHeight()/20, Gdx.graphics.getWidth()/30);
                enemyCircles3[i] = new Circle(enemyX[i]+ Gdx.graphics.getWidth()/30,  Gdx.graphics.getHeight()/2 + enemyOfSet3[i] + Gdx.graphics.getHeight()/20, Gdx.graphics.getWidth()/30);

            }


            // Kuşun düşüş hareketi kontrol edilir.
            if(birdY>0){
                velocity=velocity+gravity; // Yerçekimi uygulanır.
                birdY=birdY-velocity; // Kuş aşağı doğru hareket eder.
            }else{
                gameState =2; // Kuş yere çarptığında oyun durumu sona erer.
            }

        }else if(gameState == 0) { // Oyun başlangıç durumunda.
            if(Gdx.input.justTouched()){
                gameState = 1; // Oyuncu dokunduğunda oyun başlar.
            }
        }else if (gameState == 2){ // Oyun bittiğinde.
            // Oyun bitti mesajı ekrana çizilir.
            font2.draw(batch, "Game Over! Tape To Play Again!",300, Gdx.graphics.getHeight()/2 );

            // Oyuncu yeniden başlatmak için ekrana dokunursa oyun sıfırlanır.
            if(Gdx.input.justTouched()){
                // Oyunun durumu oyun devam ediyor (gameState = 1) olarak ayarlanır.
                gameState = 1;
                // Kuşun Y eksenindeki başlangıç konumu ayarlanır.
                birdY = Gdx.graphics.getHeight()/3;

                // Tüm arılar sıfırlanır ve yeni konumlar atanır.
                for (int i = 0; i<numberOfEnemies; i++){

                    // Arıların Y eksenindeki konumları rastgele yeniden belirlenir.
                    enemyOfSet[i] = (random.nextFloat() - 0.5f) * (Gdx.graphics.getHeight() - 200);
                    enemyOfSet2[i] = (random.nextFloat() - 0.5f) * (Gdx.graphics.getHeight() - 200);
                    enemyOfSet3[i] = (random.nextFloat() - 0.5f) * (Gdx.graphics.getHeight() - 200);

                    // Arıların X eksenindeki başlangıç konumları yeniden ayarlanır.
                    enemyX[i] = Gdx.graphics.getWidth() - bee1.getWidth()/2 + i*distance;

                    // Arı çemberleri sıfırlanır ve yeniden oluşturulur.
                    enemyCircles[i] = new Circle();
                    enemyCircles2[i] = new Circle();
                    enemyCircles3[i] = new Circle();
                }

                // Kuşun düşüş hızı sıfırlanır.
                velocity = 0;
                // Skor ve skorEnemy sıfırlanır.
                scoreEnemy = 0;
                score = 0;

            }
        }

        // Kuş ve skor ekrana çizilir.
        // Kuş görseli ekrana çizilir. Konumu X ve Y eksenine göre ayarlanır.
        batch.draw(bird,birdX, birdY, Gdx.graphics.getWidth()/15, Gdx.graphics.getHeight()/10);
        // Oyuncunun puanı ekrana çizilir.
        // Skor, belirli bir konuma (100,100) çizilir ve oyuncuya gösterilir.
        font.draw(batch, String.valueOf(score), 100,100);

        // Grafiklerin çizimi tamamlanır.
        batch.end();

        // Kuşun çarpışma çemberi güncellenir.
        // Kuşun merkez noktası ve yarıçapı ayarlanır.
        birdCircle.set(birdX + Gdx.graphics.getWidth()/30, birdY + Gdx.graphics.getHeight()/20,Gdx.graphics.getWidth()/30);



        //shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        //shapeRenderer.setColor(Color.BLACK);
        //shapeRenderer.circle(birdCircle.x, birdCircle.y, birdCircle.radius);




        // Çarpışma kontrolleri yapılır.
        // Tüm arılar için bir döngü başlatılır.
        for(int i = 0; i < numberOfEnemies; i++){
            //shapeRenderer.circle(enemyX[i]+ Gdx.graphics.getWidth()/30,  Gdx.graphics.getHeight()/2 + enemyOfSet[i] + Gdx.graphics.getHeight()/20, Gdx.graphics.getWidth()/30);
            //shapeRenderer.circle(enemyX[i]+ Gdx.graphics.getWidth()/30,  Gdx.graphics.getHeight()/2 + enemyOfSet2[i] + Gdx.graphics.getHeight()/20, Gdx.graphics.getWidth()/30);
            //shapeRenderer.circle(enemyX[i]+ Gdx.graphics.getWidth()/30,  Gdx.graphics.getHeight()/2 + enemyOfSet3[i] + Gdx.graphics.getHeight()/20, Gdx.graphics.getWidth()/30);

            // Kuşun çemberi ve arı çemberleri arasındaki çarpışma kontrol edilir.
            if(Intersector.overlaps(birdCircle, enemyCircles[i]) || Intersector.overlaps(birdCircle, enemyCircles2[i]) || Intersector.overlaps(birdCircle, enemyCircles3[i])){
                // Çarpışma algılanırsa oyun durumu sona erer (gameState = 2).
                gameState = 2; // Çarpışma algılanırsa oyun sona erer.
            }
        }
        //shapeRenderer.end();
    }

    //Kaynaklar serbest bırakılır.
    @Override
    public void dispose() {

    }
}
