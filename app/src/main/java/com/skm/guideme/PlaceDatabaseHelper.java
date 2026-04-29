package com.skm.guideme;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PlaceDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "places.db";
    private static final int DB_VERSION = 1;

    public PlaceDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE places (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "place_name TEXT UNIQUE, " +
                "category TEXT, " +
                "description TEXT, " +
                "image_name TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS places");
        onCreate(db);
    }

    /**
     * Inserts or updates a place in the database.
     */
    public void insertOrUpdatePlace(String placeName, String category, String description, String imageName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("place_name", placeName);
        values.put("category", category);
        values.put("description", description);
        values.put("image_name", imageName);

        int rowsAffected = db.update("places", values, "place_name=? AND category=?", new String[]{placeName, category});
        if (rowsAffected == 0) {
            db.insert("places", null, values);
        }
    }

    /**
     * Gets the details of a place by name.
     */
    public Place getPlaceDetails(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM places WHERE place_name=?", new String[]{name});

        if (cursor.moveToFirst()) {
            String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
            String imageName = cursor.getString(cursor.getColumnIndexOrThrow("image_name"));
            cursor.close();
            return new Place(name, description, imageName);
        } else {
            cursor.close();
            return null;
        }
    }

    public String getDescription(String placeName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT description FROM places WHERE place_name = ?", new String[]{placeName});
        String desc = "No description available.";
        if (cursor.moveToFirst()) {
            desc = cursor.getString(0);
        }
        cursor.close();
        return desc;
    }

    /**
     * Inserts initial hotel data.
     */
    public void insertHotelData(Context context) {
        // Use instance method insertOrUpdatePlace directly
        insertOrUpdatePlace("Hotel Indreni And Lodge", "hotels", "A modern hotel offering comfortable rooms and peaceful ambiance. Great for business and leisure travelers.", "hotelindreni.png");
        insertOrUpdatePlace("Hotel Happy Stay", "hotels", "Budget-friendly hotel with a warm, homey feel. Suitable for short stays or quick stopovers.", "hotelhappystay.png");
        insertOrUpdatePlace("Hotel Timurti Pvt. Ltd", "hotels", "A newly opened hotel with clean rooms and basic amenities. Focuses on guest satisfaction and hygiene.", "hoteltimurti.png");
        insertOrUpdatePlace("Lalit Bhojanalaya", "hotels", "A cozy local restaurant with traditional Nepali cuisine. Popular among locals and travelers for its authentic flavors.", "lalitbhojanalaya.png");
        insertOrUpdatePlace("Lumbini Heritage Home", "hotels", "A heritage-style guesthouse offering warm hospitality. Perfect for visitors seeking a homely atmosphere.", "lumbiniheritagehome.png");
        insertOrUpdatePlace("Sayami Home", "hotels", "Comfortable lodging with friendly service in a convenient location. Ideal for budget-conscious travelers.", "sayamihome.png");
        insertOrUpdatePlace("Hotel Goodwill", "hotels", "A well-maintained hotel offering clean rooms and good amenities. Known for attentive customer care.", "hotelgoodwill.png");
        insertOrUpdatePlace("Hotel Sunset View", "hotels", "Scenic views combined with comfortable accommodation. A great spot to relax and enjoy the sunset.", "hotelsunsetview.png");
        insertOrUpdatePlace("HOTEL RANI MAHAL PVT. LTD.", "hotels", "A private hotel providing quality services and modern facilities. Suited for both leisure and business stays.", "hotelranimahalpvtltd.png");
        insertOrUpdatePlace("ARYAL INT'L HOTEL", "hotels", "An international standard hotel with spacious rooms and professional staff. Conveniently located for travelers.", "aryalintlhotel.png");
        insertOrUpdatePlace("Hong Xiang Hotel", "hotels", "Comfortable rooms with Asian hospitality. Popular among tourists for its welcoming atmosphere.", "hongxianghotel.png");
        insertOrUpdatePlace("CG Group Of Hotels Vivanta Kathmandu", "hotels", "A premium luxury hotel operated by CG Group. Known for its world-class service and rooftop views.", "cggroupofhotelsvivantakathmandu.png");
        insertOrUpdatePlace("Hotel Kutumba", "hotels", "A modern hotel offering stylish rooms and traditional ambiance. Located conveniently near central Patan.", "hotelkutumba.png");
        insertOrUpdatePlace("Lumbini Tanduri Bhojanalaya", "hotels", "A local favorite serving authentic tandoori cuisine. Offers basic lodging with flavorful meals.", "lumbinitanduribhojanalaya.png");
        insertOrUpdatePlace("Saligram Apartment Hotel", "hotels", "An apartment-style hotel perfect for long stays. Combines comfort of home with hotel service.", "saligramapartmenthotel.png");
        insertOrUpdatePlace("Shangri- la blu Hotel", "hotels", "A boutique hotel with elegant décor and calm atmosphere. Ideal for business and leisure travelers.", "shangrilabluhotel.png");
        insertOrUpdatePlace("Shree Bageswori Hotel", "hotels", "Budget-friendly accommodation with clean rooms. Popular among pilgrims and short-term visitors.", "shreebagesworihotel.png");
        insertOrUpdatePlace("Hotel Trimurti Pvt. Ltd", "hotels", "A peaceful hotel offering neat rooms and friendly service. Ideal for travelers seeking budget comfort.", "hoteltrimurtipvtltd.png");
        insertOrUpdatePlace("Himalaya Apartment Hotel", "hotels", "A comfortable hotel offering spacious apartment-style rooms with modern amenities, ideal for both short and long stays in a peaceful environment.", "himalayaapartmenthotel.png");
        insertOrUpdatePlace("Crown Plaza", "hotels", "Permanently closed.", "error_image.png");
        insertOrUpdatePlace("Vivantaa Hotel", "hotels", "Permanently closed.", "error_image.png");
        insertOrUpdatePlace("Anisha Shrestha Hotel", "hotels", "Permanently closed.", "error_image.png");
        insertOrUpdatePlace("3 rooms by Pauline", "hotels", "Permanently closed.", "error_image.png");
        insertOrUpdatePlace("Hotel Red Rooster", "hotels", "Permanently closed.", "error_image.png");
        insertOrUpdatePlace("Surya Homestay", "hotels", "Permanently closed.", "error_image.png");
    }

    /**
     * Inserts hospital data.
     */
    public void insertHospitalData(Context context) {
        // Hospitals
        insertOrUpdatePlace("ग्लोबल अस्पताल", "hospitals", "A private hospital providing advanced technology-based services. Renowned for its emergency care.", "globalhospital.png");
        insertOrUpdatePlace("भिजन पोलिक्लिनिक", "hospitals", "A multi-specialty clinic serving at the local level. Provides quality services at affordable rates.", "visionpolyclinicpvtltd.png");
        insertOrUpdatePlace("एपेक्स पोलिक्लिनिक एण्ड डायग्नोसिस सेन्टर प्रा.लि.", "hospitals", "Expert in various diagnostic and testing services. Well-equipped and easily accessible.", "apex.png");
        insertOrUpdatePlace("हाम्रो सहयात्री अस्पताल", "hospitals", "Established to address the lack of local health services, accessible to the general public. Known for cleanliness and friendly environment.", "hamrosahayatrihospital.png");
        insertOrUpdatePlace("नेचर केयर अस्पताल", "hospitals", "A hospital specialized in natural healing methods. Focused on fast recovery of patients.", "naturecarehospital.png");
        insertOrUpdatePlace("नेत्रधाम आँखा सेवा केन्द्र", "hospitals", "A center providing specialized eye treatment services. Utilizes the latest technologies.", "netradhamaakhasewakendra.png");
        insertOrUpdatePlace("निदान अस्पताल प्रा.लि.", "hospitals", "A multi-specialty diagnostic and treatment center located in Lalitpur.", "nidanhospitalpvtltd.png");
        insertOrUpdatePlace("आरोग्य स्वस्थ्य सदन", "hospitals", "Provides general healthcare services with a focus on community well-being.", "aarogyahealthhome.png");
        insertOrUpdatePlace("अल्का अस्पताल प्रा.लि.", "hospitals", "A well-known private hospital offering a wide range of medical services.", "alkahospitalpvtltd.png");
        insertOrUpdatePlace("रिदम न्यूरोसाईकियाट्री अस्पताल र अनुसन्धान केन्द्र", "hospitals", "A specialized center for neuropsychiatric treatment and research.", "ridamneuropsychiatryhospital.png");
        insertOrUpdatePlace("गणेश मान सिंह मेमोरियल अस्पताल तथा अनुसन्धान केन्द्र", "hospitals", "A renowned memorial hospital dedicated to research and modern healthcare.", "ganeshmansinghhospital.png");
    }

    /**
     * Inserts bank data.
     */
    public void insertBankData(Context context) {
        insertOrUpdatePlace("प्रभु बैंक", "banks", "A leading bank providing services across Nepal. Known for fast and secure banking.", "prabhubank.png");
        insertOrUpdatePlace("Siddhartha Bank", "banks", "Siddhartha Bank offers excellent digital banking services. Focused on customer satisfaction.", "siddharthabank.png");
        insertOrUpdatePlace("Nabil Bank Pulchok", "banks", "The oldest and most reputable bank in Nepal. Expert in international services and card systems.", "nabilbankpulchok.png");
        insertOrUpdatePlace("Nirdhan Utthan Bank Tikathali Branch", "banks", "A microfinance bank for small and medium enterprises. Expanding access in rural and urban areas.", "nirdhanutthan.png");
        insertOrUpdatePlace("Buddha Saving and Credit Cooperative", "banks", "A cooperative institution based on group savings and credit services. Trusted in the local community.", "buddhasaving.png");
        insertOrUpdatePlace("Jyoti Bikash Bank", "banks", "A development bank with innovative services. A good choice for digital transactions.", "jyotibank.png");
        insertOrUpdatePlace("Taranpunja Saving and Credit Co-operative Society Ltd", "banks", "A financial institution run on cooperative principles. Provides regular savings and loan services.", "taranpunjasaving.png");
        insertOrUpdatePlace("Gautam Samaj Saving And Credit Cooperative Ltd", "banks", "A financial institution operated by Gautam Samaj. Actively contributing to local development.", "gautamsamaj.png");
        insertOrUpdatePlace("सिद्धार्थ बैंक", "banks", "A leading bank in Nepal offering digital services. Provides easy banking for customers.(ATM)", "siddardhabankatm.png");
        insertOrUpdatePlace("Remit2Nepal", "banks", "A reliable and popular remittance company for fast and secure money transfers to Nepal.", "remit2nepal.png");
        insertOrUpdatePlace("Nabil Bank", "banks", "A branch of Nepal’s oldest and most reputed bank. Known for excellent customer service and financial solutions.", "nabilbank.png");
        insertOrUpdatePlace("Civil Bank Limited", "banks", "A public investment bank focused on digital banking. Known for convenient services.", "civilbankltd.png");
        insertOrUpdatePlace("नेपाल राष्ट्र बैंक", "banks", "Nepal’s central bank that formulates monetary policy. Plays a key role in regulating the banking sector.", "nepalrastrabank.png");
        insertOrUpdatePlace("Nepal Bangladesh Bank Head Office", "banks", "A joint venture bank between Nepal and Bangladesh. Excellent in both business and personal services.", "nepalbangladeshbankheadoffice.png");
        insertOrUpdatePlace("नेपाल इन्फ्रास्ट्रक्चर बैंक लिमिटेड", "banks", "A bank specializing in financing infrastructure projects. Dedicated to sustainable development.", "nepalinfrastructurebanklimited.png");
        insertOrUpdatePlace("सानिमा बैंक", "banks", "A bank offering the latest digital banking services. Focused on customer satisfaction.", "sanimabank.png");
    }

    /**
     * Inserts tourist attraction data.
     */
    
    public void insertTouristAttractionData(Context context) {
        insertOrUpdatePlace("Stone spout", "tourism", "A historic water spout with cultural significance. Popular spot for both locals and tourists.", "stonespout.png");
        insertOrUpdatePlace("पाटन दरवार क्षेत्र", "tourism", "A place of historical and cultural significance. One of the main attractions for tourists. It is one of the three Durbar Squares in the Kathmandu Valley, all of which are UNESCO World Heritage Sites. One of its attractions is the medieval royal palace where the Malla Kings of Lalitpur resided.", "patandarbarxetra.png");
    }
}

