package com.example.ai

import android.graphics.Bitmap
import android.util.Base64
import com.aistudio.ameen.medication.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

data class DrugInfo(
    val brandName: String,
    val genericName: String,
    val commonDosage: String,
    val unit: String,
    val timingRule: String,
    val indications: String,
    val contraindications: String,
    val foodAndDrugInteractions: String,
    val sideEffects: String,
    val isCritical: Boolean = false,
    val isPRN: Boolean = false,
    val isLocal: Boolean = true,
    val disclaimer: String = "⚠️ هذه المعلومات للاسترشاد الطبي فقط ولا تغني إطلاقاً عن استشارة الطبيب أو الصيدلي المختص."
)

data class ExtractedMedicationItem(
    val name: String,
    val dosage: String = "1",
    val unit: String = "قرص",
    val frequency: Int = 1,
    val timing: String = "مع الأكل",
    val instructions: String = ""
)

data class PrescriptionExtractResult(
    val doctorName: String?,
    val clinicOrHospital: String?,
    val dateIssued: String,
    val diagnosis: String?,
    val medicationsSummary: String,
    val medications: List<ExtractedMedicationItem>,
    val disclaimer: String = "⚠️ بيانات مستخرجة بواسطة الذكاء الاصطناعي ويجب مراجعتها وتعديلها بشرياً قبل الحفظ."
)

object DrugKnowledgeService {

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val localDatabase = listOf(
        DrugInfo(
            brandName = "أملوديبين",
            genericName = "Amlodipine Besylate",
            commonDosage = "5",
            unit = "مجم (قرص)",
            timingRule = "مع الأكل أو بعد الإفطار",
            indications = "علاج ارتفاع ضغط الدم والذبحة الصدرية المزمنة",
            contraindications = "الحساسية لمثبطات قنوات الكالسيوم، الهبوط الحاد بضغط الدم",
            foodAndDrugInteractions = "تجنب تناول الجريب فروت مع الدواء لأنه يزيد تركيزه بالدم. الحذر مع أدوية الكلى.",
            sideEffects = "تورم طفيف بالقدمين أو الكاحل، دوار عند الوقوف السريع",
            isCritical = true
        ),
        DrugInfo(
            brandName = "كونكور",
            genericName = "Bisoprolol Fumarate",
            commonDosage = "2.5",
            unit = "مجم (قرص)",
            timingRule = "صباحاً قبل أو بعد الإفطار في نفس الموعد",
            indications = "تنظيم ضربات القلب، علاج ارتفاع ضغط الدم وفشل عضلة القلب",
            contraindications = "الربو الحاد، بطء ضربات القلب الشديد (أقل من 50 نبضة/دقيقة)",
            foodAndDrugInteractions = "يتعارض مع أدوية الكالسيوم مثل ديلتيازيم وفيراباميل",
            sideEffects = "برودة بالأطراف، إرهاق خفيف أول أسبوعين",
            isCritical = true
        ),
        DrugInfo(
            brandName = "جلوكوفاج",
            genericName = "Metformin HCl",
            commonDosage = "1000",
            unit = "مجم (قرص)",
            timingRule = "وسط الوجبة الرئيسية مباشرة",
            indications = "علاج مرض السكري من النوع الثاني ومقاومة الأنسولين",
            contraindications = "القصور الكلوي الشديد، الحماض الكيتوني",
            foodAndDrugInteractions = "تجنب تناول المشروبات السكرية بكميات عالية والمشروبات الكحولية",
            sideEffects = "اضطرابات هضمية، إسهال خفيف يقل بالتدريج",
            isCritical = true
        ),
        DrugInfo(
            brandName = "بانادول إكسترا",
            genericName = "Paracetamol + Caffeine",
            commonDosage = "500",
            unit = "مجم (أقراص)",
            timingRule = "بعد الطعام عند اللزوم",
            indications = "مسكن للصداع الحاد، آلام الأسنان، خافض للحرارة",
            contraindications = "أمراض الكبد الحادة، الحساسية للباراسيتامول",
            foodAndDrugInteractions = "التقليل من المنبهات (القهوة/الشاي) لتجنب زيادة ضربات القلب بسبب الكافيين",
            sideEffects = "أرق خفيف عند تناوله ليلاً",
            isPRN = true
        ),
        DrugInfo(
            brandName = "أوجمنتين",
            genericName = "Amoxicillin + Clavulanic Acid",
            commonDosage = "1000",
            unit = "مجم (قرص)",
            timingRule = "في بداية الوجبة لتقليل اضطراب المعدة",
            indications = "مضاد حيوي واسع المجال لعدوى الجهاز التنفسي والأذن والمسالك",
            contraindications = "حساسية البنسلين، مشاكل كبدية سابقة مع الأموكسيسيلين",
            foodAndDrugInteractions = "قد يقلل من فاعلية حبوب منع الحمل. تجنب تناوله مع ميثوتريكسات",
            sideEffects = "إسهال، غثيان خفيف",
            isCritical = false
        ),
        DrugInfo(
            brandName = "إلتروكسين",
            genericName = "Levothyroxine Sodium",
            commonDosage = "50",
            unit = "ميكروجرام (قرص)",
            timingRule = "على معدة فارغة تماماً قبل الإفطار بـ 30-60 دقيقة مع كوب ماء",
            indications = "علاج قصور وخمول الغدة الدرقية",
            contraindications = "التسمم الدرقي غير المعالج، احتشاء عضلة القلب الحاد",
            foodAndDrugInteractions = "فصل الدواء 4 ساعات على الأقل عن مكملات الكالسيوم أو الحديد أو القهوة",
            sideEffects = "خفقان أو أرق في حال كانت الجرعة زائدة",
            isCritical = true
        ),
        DrugInfo(
            brandName = "نيكسيوم",
            genericName = "Esomeprazole",
            commonDosage = "40",
            unit = "مجم (قرص)",
            timingRule = "قبل الإفطار بنصف ساعة",
            indications = "علاج ارتجاع المريء وقرحة المعدة وحرقة الفؤاد",
            contraindications = "الحساسية لمثبطات مضخة البروتون",
            foodAndDrugInteractions = "يقلل امتصاص فيتامين B12 ومكملات الحديد مع الاستخدام الطويل",
            sideEffects = "صداع، إمساك أو إسهال مؤقت",
            isCritical = false
        ),
        DrugInfo(
            brandName = "فيتامين D3",
            genericName = "Cholecalciferol",
            commonDosage = "5000",
            unit = "وحدة دولية (كبسولة)",
            timingRule = "مع أو بعد وجبة غنية بالدهون الصحية",
            indications = "علاج نقص فيتامين د، دعم المناعة وتقوية العظام",
            contraindications = "فرط كالسيوم الدم، حصوات الكلى الكلسية الشديدة",
            foodAndDrugInteractions = "مدرات البول الثيازيدية قد تزيد الكالسيوم بالدم",
            sideEffects = "نادرة جداً بالجرعات الطبيعية",
            isCritical = false
        ),
        DrugInfo(
            brandName = "بخاخ فنتولين",
            genericName = "Salbutamol Inhaler",
            commonDosage = "100",
            unit = "ميكروجرام (بخة)",
            timingRule = "عند اللزوم / عند ضيق التنفس",
            indications = "موسع للشعب الهوائية سريع المفعول لأزمات الربو",
            contraindications = "الحساسية للمادة الفعالة",
            foodAndDrugInteractions = "مثبطات بيتا غير الانتقائية (مثل بروبرانولول) تعاكس مفعوله",
            sideEffects = "رعشة خفيفة باليدين، تسارع مؤقت بنبض القلب",
            isPRN = true
        ),
        DrugInfo(
            brandName = "ليبيتور",
            genericName = "Atorvastatin",
            commonDosage = "20",
            unit = "مجم (قرص)",
            timingRule = "مساءً قبل النوم",
            indications = "خفض الكوليسترول الضار والدهون الثلاثية والوقاية من جلطات القلب",
            contraindications = "أمراض الكبد النشطة، الحمل والرضاعة",
            foodAndDrugInteractions = "تجنب عصير الجريب فروت بكميات كبيرة",
            sideEffects = "آلام عضلية خفيفة أحياناً",
            isCritical = true
        )
    )

    /**
     * Search local database first
     */
    fun searchLocal(query: String): List<DrugInfo> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return emptyList()
        return localDatabase.filter {
            it.brandName.lowercase().contains(q) ||
            it.genericName.lowercase().contains(q) ||
            it.indications.lowercase().contains(q)
        }
    }

    /**
     * Search drug information using Gemini 3.5 Flash REST API with clear Arabic medical disclaimers
     */
    suspend fun searchOnlineOrAi(query: String): Result<DrugInfo> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException("⚠️ يلزم إعداد مفتاح GEMINI_API_KEY في ملف .env أو إعدادات التطبيق لتفعيل البحث بالذكاء الاصطناعي.")
            )
        }

        val prompt = """
            أنت استشاري صيدلي ومساعد دوائي دقيق وموثوق.
            قدم معلومات دوائية كاملة باللغة العربية للدواء التالي: "$query".
            أرجع النتيجة بصيغة JSON فقط متطابقة مع البنية التالية وبدون أي نصوص إضافية:
            {
              "brandName": "الاسم التجاري الشائع بالعربية",
              "genericName": "الاسم العلمي / المادة الفعالة بالإنجليزية والعربية",
              "commonDosage": "500",
              "unit": "مجم",
              "timingRule": "مع الأكل",
              "indications": "دواعي الاستعمال الطبية الشائعة",
              "contraindications": "موانع الاستعمال والتحذيرات الهامة",
              "foodAndDrugInteractions": "التداخلات الغذائية والدوائية",
              "sideEffects": "الآثار الجانبية المحتملة",
              "isCritical": false,
              "isPRN": false
            }
        """.trimIndent()

        try {
            val requestJson = JSONObject().apply {
                val contents = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                        }
                        put("parts", parts)
                    }
                    put(contentObj)
                }
                put("contents", contents)
            }

            val requestBody = requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                val code = response.code
                val errBody = response.body?.string() ?: ""
                return@withContext Result.failure(
                    when (code) {
                        429 -> Exception("تم تجاوز حد الاستخدام المسموح به لـ Gemini API حالياً (429). يرجى المحاولة لاحقاً.")
                        400, 403 -> Exception("مفتاح Gemini API غير صالح أو غير مصرح له بالوصول ($code).")
                        else -> Exception("خطأ من خادم الذكاء الاصطناعي ($code): $errBody")
                    }
                )
            }

            val respString = response.body?.string() ?: ""
            val root = JSONObject(respString)
            val candidates = root.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext Result.failure(Exception("لم يُرجع الذكاء الاصطناعي أي بيانات عن هذا الدواء."))
            }

            val text = candidates.getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .optString("text", "")

            // Clean code fences if returned
            val cleanJson = text.replace("```json", "").replace("```", "").trim()
            val drugObj = JSONObject(cleanJson)

            val info = DrugInfo(
                brandName = drugObj.optString("brandName", query),
                genericName = drugObj.optString("genericName", ""),
                commonDosage = drugObj.optString("commonDosage", "1"),
                unit = drugObj.optString("unit", "قرص"),
                timingRule = drugObj.optString("timingRule", "مع الأكل"),
                indications = drugObj.optString("indications", ""),
                contraindications = drugObj.optString("contraindications", ""),
                foodAndDrugInteractions = drugObj.optString("foodAndDrugInteractions", ""),
                sideEffects = drugObj.optString("sideEffects", ""),
                isCritical = drugObj.optBoolean("isCritical", false),
                isPRN = drugObj.optBoolean("isPRN", false),
                isLocal = false
            )
            Result.success(info)
        } catch (e: UnknownHostException) {
            Result.failure(Exception("تعذر الاتصال بالإنترنت، يرجى التحقق من اتصال الشبكة والمحاولة مجدداً."))
        } catch (e: SocketTimeoutException) {
            Result.failure(Exception("انتهت مهلة الاتصال بخادم الذكاء الاصطناعي. يرجى إعادة المحاولة."))
        } catch (e: Exception) {
            Result.failure(Exception("حدث خطأ أثناء معالجة بيانات الدواء: ${e.localizedMessage}"))
        }
    }

    /**
     * Smart OCR: parse medical prescription image using Gemini 3.5 Flash Vision
     * Requires human confirmation in UI before saving.
     */
    suspend fun parsePrescriptionAi(bitmap: Bitmap): Result<PrescriptionExtractResult> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException("⚠️ يلزم إعداد مفتاح GEMINI_API_KEY لتفعيل قراءة الروشتة بالذكاء الاصطناعي.")
            )
        }

        try {
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos)
            val imageBytes = baos.toByteArray()
            val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

            val prompt = """
                أنت استشاري طبي وصيدلي خبير. قم بقراءة وتحليل صورة الروشتة الطبية بعناية واستخرج البيانات التالية باللغة العربية.
                أرجع النتيجة حصراً بصيغة JSON بدون أي نصوص قبلها أو بعدها أو markdown fences:
                {
                  "doctorName": "اسم الطبيب إن وجد أو فارغ",
                  "clinicOrHospital": "اسم المستشفى أو العيادة أو فارغ",
                  "dateIssued": "تاريخ كتابة الروشتة",
                  "diagnosis": "التشخيص أو الملاحظات الطبية إن وجدت",
                  "medicationsSummary": "ملخص كامل للأدوية والتعليمات المكتوبة بالروشتة",
                  "medications": [
                    {
                      "name": "اسم الدواء",
                      "dosage": "الجرعة مثلا 500 أو 1",
                      "unit": "قرص أو مل أو كبسولة أو مجم",
                      "frequency": 1,
                      "timing": "مع الأكل أو قبل الأكل أو بعد الأكل",
                      "instructions": "تعليمات الاستخدام المكتوبة"
                    }
                  ]
                }
            """.trimIndent()

            val requestJson = JSONObject().apply {
                val contents = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val parts = JSONArray().apply {
                            // Image part
                            val inlineData = JSONObject().apply {
                                put("mimeType", "image/jpeg")
                                put("data", base64Image)
                            }
                            put(JSONObject().put("inlineData", inlineData))
                            // Text prompt
                            put(JSONObject().put("text", prompt))
                        }
                        put("parts", parts)
                    }
                    put(contentObj)
                }
                put("contents", contents)
            }

            val requestBody = requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                val code = response.code
                return@withContext Result.failure(Exception("فشلت قراءة الروشتة بالذكاء الاصطناعي ($code)."))
            }

            val respString = response.body?.string() ?: ""
            val root = JSONObject(respString)
            val candidates = root.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext Result.failure(Exception("لم يتمكن الذكاء الاصطناعي من قراءة تفاصيل الصورة."))
            }

            val text = candidates.getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .optString("text", "")

            val cleanJson = text.replace("```json", "").replace("```", "").trim()
            val parsedObj = JSONObject(cleanJson)

            val medList = mutableListOf<ExtractedMedicationItem>()
            val medsArray = parsedObj.optJSONArray("medications")
            if (medsArray != null) {
                for (i in 0 until medsArray.length()) {
                    val m = medsArray.getJSONObject(i)
                    medList.add(
                        ExtractedMedicationItem(
                            name = m.optString("name", "دواء غير مسمى"),
                            dosage = m.optString("dosage", "1"),
                            unit = m.optString("unit", "قرص"),
                            frequency = m.optInt("frequency", 1),
                            timing = m.optString("timing", "مع الأكل"),
                            instructions = m.optString("instructions", "")
                        )
                    )
                }
            }

            val result = PrescriptionExtractResult(
                doctorName = parsedObj.optString("doctorName").ifBlank { null },
                clinicOrHospital = parsedObj.optString("clinicOrHospital").ifBlank { null },
                dateIssued = parsedObj.optString("dateIssued", ""),
                diagnosis = parsedObj.optString("diagnosis").ifBlank { null },
                medicationsSummary = parsedObj.optString("medicationsSummary", ""),
                medications = medList
            )
            Result.success(result)
        } catch (e: UnknownHostException) {
            Result.failure(Exception("تعذر الاتصال بالشبكة لقراءة الروشتة."))
        } catch (e: Exception) {
            Result.failure(Exception("خطأ أثناء استخراج بيانات الروشتة: ${e.localizedMessage}"))
        }
    }
}
