import java.util.concurrent.Semaphore;

public class Filozof implements Runnable {
    private int filozofId;
    // Sol ve sağ çatalı temsil eden semaforlar. Niye semafor türünde değişkenleri tanımlıyoruz? Çünkü her bir çatalı bir thread alıp serbest bırakacak.
    private Semaphore solCatal;
    private Semaphore sagCatal;
    // Her bir filozofun kaç kez yemek yediğini tutan dizi. 5 filozof olduğu için 5 elemanlı bir dizi.
    private static int[] yemekSayiDizisi = new int[5];

    public Filozof(int filozofId, Semaphore solCatal, Semaphore sagCatal) {
        this.filozofId = filozofId;
        this.solCatal = solCatal;
        this.sagCatal = sagCatal;
    }

    @Override
    public void run() {
        int iterasyon = 10;
        /*
        !Thread.currentThread().isInterrupted() ifadesi, thread'in kesintiye uğrayıp uğramadığını kontrol eder.
        Bu koşul ifadesi, iş parçacığına kesinti sinyali gelmediği sürece ve iterasyon değişkeni 0'dan büyük olduğu sürece döngüyü sürdürür.
        */
        while (!Thread.currentThread().isInterrupted() && iterasyon > 0) {
            think();
            eat();
            iterasyon--;
        }
    }

    public void think() {
        System.out.println((filozofId + 1) + ".Filozof düşünüyor");
        try {
            Thread.sleep((long) (Math.random() * 3000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void eat() {
        // İlk önce sol çatalı al
        solCatal.acquireUninterruptibly(); // acquireUninterruptibly() metodu, kesintiye uğramadan semaforu almaya çalışır

        // Ardından sağ çatalı almaya çalış
        if (sagCatal.tryAcquire()) {
            // İki çatalı da aldın, yemek yeme işlemini gerçekleştir ve ilgili filozofun yediği yemek sayısını arttır
            System.out.println((filozofId + 1) + ".Filozof yemek yiyor");
            increaseMealCount(filozofId);

            // Her iki çatalı da yemek yedikten sonra serbest bırak
            sagCatal.release();
            solCatal.release();
        } else {
            // Sağ çatalı alamadın, sol çatalı serbest bırak
            solCatal.release();
        }

        // Yemek yedikten sonra bir süre bekle
        try {
            Thread.sleep((long) (Math.random() * 3000));
        } catch (InterruptedException e) { // Thread.sleep() metodu, kesintiye uğrarsa InterruptedException fırlatır
            Thread.currentThread().interrupt(); // Thread.currentThread().interrupt() metodu, kesintiye uğrayan thread'in kesinti durumunu true yapar
        }
    }

    // İlgili filozofun yediği yemek sayısını arttır
    private void increaseMealCount(int filozofId) {
        yemekSayiDizisi[filozofId] += 1;
    }

    public static void main(String[] args) {
        Semaphore[] catalDizisi = new Semaphore[5]; // Her bir filozofun sol ve sağ çatalını temsil eden semafor dizisi. Toplam 5 adet çatal var.
        for (int i = 0; i < 5; i++) {
            catalDizisi[i] = new Semaphore(1); // Her bir çatalı temsil eden semaforu 1 olarak başlat. Yani her çatal başlangıçta serbest.
        }

        Thread[] filozofThreadDizisi = new Thread[5]; // Her bir filozofu temsil eden thread dizisi. Toplam 5 filozof var.

        // Her bir filozofu temsil eden thread'i başlat
        for (int i = 0; i < 5; i++) {
            filozofThreadDizisi[i] = new Thread(new Filozof(i, catalDizisi[i], catalDizisi[(i + 1) % 5])); // Her bir filozofun sol çatalı kendisine, sağ çatalı ise kendisinden bir sonraki filozofa ait
            filozofThreadDizisi[i].start();
        }

        // 10 saniye boyunca programı çalıştır
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Her bir filozofu temsil eden thread'i sonlandır
        for (Thread thread : filozofThreadDizisi) {
            thread.interrupt();
        }

        // Her bir filozofun kaç kez yemek yediğini ekrana yazdır
        for (int i = 0; i < 5; i++) {
            System.out.println("Filozof-" + (i + 1) + " --> " + yemekSayiDizisi[i] + " kez yemek yedi");
        }
    }
}