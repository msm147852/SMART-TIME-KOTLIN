# SMART TIME — الدليل التشريحي التفصيلي لقسم المصروفات والمشتريات والدخل
**وثيقة تنفيذ ومطابقة UI/UX + Business Logic + Data + API**  
تاريخ التوثيق: 2026-11-02  
المستودع الأصلي: `msm147852/SMART-TIME-` — الفرع الافتراضي الذي تمت القراءة منه  
المستودع المستهدف: `msm147852/SMART-TIME-KOTLIN`  
النطاق: قسم المصروفات والمشتريات والشؤون المالية بالكامل، بما يشمل كل شاشة فرعية ونموذج إدخال وأزرار CRUD والتقارير والدخل والشهادات والحساب الجاري ودخل السيارة.

---

# 0. تعليمات قراءة هذه الوثيقة

هذه الوثيقة توثق ما أمكن قراءته مباشرة من ملفات المصدر المذكورة أدناه. وهي تفرق بين:
- **مؤكد من JSX/TypeScript:** اسم العنصر، موضعه النسبي، النص الظاهر، الـhandler، والبيانات المستخدمة.
- **قاعدة حساب مؤكدة:** معادلة أو تحويل موجود في الدوال المصدرية.
- **مطلوب استكمال استخراجه:** تفاصيل داخل مكونات فرعية أو نماذج قديمة أو أجزاء غير ظاهرة في الملف المقروء.

لا تُحوّل وصفًا عامًا إلى وظيفة مفترضة. إذا كان الزر أيقونة فقط، اذكر اسمه البرمجي/أيقونته والـhandler بدل اختراع نص ظاهر. مواضع الأزرار موصوفة **بالترتيب المكاني داخل الشاشة** (رأس/يمين/يسار/أعلى/أسفل/داخل البطاقة) لأن المشروع Responsive ولا يستخدم إحداثيات ثابتة تصلح لكل مقاس.

**قاعدة النقل:** لا تحذف ميزة ولا تستبدلها ببطاقة ثابتة. المطلوب شاشة Kotlin Native بنفس التسلسل والسلوك والبيانات. ولا تُعدّل مستودع React الأصلي.

# 1. خريطة الملفات والأدوار

| الملف | دوره في القسم |
|---|---|
| `src/components/ExpensesView.tsx` | الحاوية الرئيسية، اختيار الشاشة، تحميل البيانات، تجميع الإجماليات، فتح النماذج وربط الأقسام |
| `src/components/expenses/FinancialDashboard.tsx` | لوحة الملخص المالي والرسوم والمؤشرات وأحدث الحركات |
| `src/components/expenses/SectionsMenuScreen.tsx` | قائمة بطاقات الأقسام وإعادة ترتيبها |
| `src/components/expenses/AddExpenseModal.tsx` | نموذج الإضافة الموحد للمصروف |
| `src/components/ExpenseEditModal.tsx` | نموذج تحرير المصروفات المتخصص/القديم؛ يجب فحص مسارات استدعائه قبل تقرير استبداله |
| `src/components/expenses/HouseExpensesSection.tsx` | مصروفات المنزل والتفاصيل المتخصصة |
| `src/components/expenses/PersonalExpensesSection.tsx` | المصروفات الشخصية |
| `src/components/expenses/WorkExpensesSection.tsx` | مصروفات العمل |
| `src/components/expenses/VehicleExpensesSection.tsx` | مصروفات المركبات |
| `src/components/expenses/EducationExpensesSection.tsx` | مصروفات التعليم |
| `src/components/expenses/IncomeAndCertificatesSection.tsx` | الدخل الشهري، الشهادات البنكية، الحساب الجاري |
| `src/components/expenses/CarIncomeSection.tsx` | دخل مشاوير السيارة وتقاريره |
| `src/components/expenses/FinancialReportsSection.tsx` | التقارير اليومية/الأسبوعية/الشهرية ومعاينة/تصدير |
| `src/components/ReportPreviewModal.tsx` | معاينة التقرير العام |
| `src/contracts/financeContract.ts` و `src/types/finance.ts` | عقد السجل المالي الموحد |
| `src/services/financeCalculations.ts` | معادلات الشهادات والدخل والشهر |
| `src/services/financeService.ts` | واجهة API للسجل المالي الموحد |
| `src/repositories/financeRepository.ts` | توحيد المصادر إلى Transaction |
| `src/services/repositories/expensesRepository.ts` | مخزن الدخل الشهري والشهادات |
| `backend/ai/finance/*` | الإسقاط المالي ومزامنة/استيراد السجلات |
| `docs/ai/EXPENSE-PHASE5-*.md` | سجل مراحل توحيد السجل المالي والتحقق |

# 2. الهيكل العام وتدفق التنقل

## 2.1 المكوّن الرئيسي
**اسم المكوّن:** `ExpensesView`  
**المسار:** `src/components/ExpensesView.tsx`  
**الحالة الرئيسية:**
- `currentScreen`: الشاشة الحالية داخل قسم المالية.
- `selectedMonth`: الشهر المحدد بصيغة `YYYY-MM`.
- `isAddExpenseModalOpen`: إظهار/إخفاء نافذة الإضافة.
- `allExpenses`: السجل الموحد المقروء.
- `incomeList`: مستندات الدخل الشهري ومصادرها.
- `certificates`: الشهادات البنكية.
- `associationsList`: بيانات الجمعيات.
- `accidentList`: سجلات حوادث المركبات.
- `students`: بيانات الطلاب المرتبطة بالتعليم.

## 2.2 الشاشات التي يديرها
- `dashboard`: لوحة المالية الافتراضية.
- `sections`: قائمة أقسام الدخل والمصروفات.
- أقسام تفصيلية تختار من القائمة: المنزل، العمل، الشخصي، المركبات، التعليم، الدخل/الشهادات، التقارير.
- نافذة `AddExpenseModal` للإضافة الموحدة.

## 2.3 التحديث والحفظ
- `refreshCanonicalExpenses()`: إعادة قراءة المصروفات الموحدة بعد التغيير.
- `handleSaveIncome(list)`: يحدث الحالة ثم `ExpensesRepository.saveMonthlyIncome(list)`.
- `handleSaveCertificates(list)`: يحدث الحالة ثم `ExpensesRepository.saveBankCertificates(list)`.
- `handleSaveAssociations(list)`: يحدث الحالة ثم `StorageAdapter.setItem('smart_time_personal_associations', list)`.
- `handleSaveAccidents(list)`: يحدث الحالة ثم `VehiclesRepository.saveAccidentRecords(list)`.
- `handleSaveStudents(list)`: يحدث الحالة ثم `StorageAdapter.setItem('smart_time_students', list)`.

## 2.4 زر الإضافة العام
الـhandler: `handleUniversalAddExpense(data)`.
المدخلات: `section, category, amount, date, paymentMethod, notes`.
تحويل النوع:
- `house` → `house`
- `work` → `work`
- `personal` → `personal`
- `education` → `student`
- `vehicle` → `vehicle_fuel` إذا احتوى التصنيف على «وقود» أو «بنزين» أو «سولار»، وإلا `vehicle_maint`.
يبني `CanonicalExpenseInput` ثم ينفذ `createCanonicalFinanceExpense(input)`، وبعد نجاح API يستدعي `refreshCanonicalExpenses()` ويغلق النافذة. عند الفشل يضع رسالة في `canonicalFinanceError`.
**ملاحظة مطابقة:** التصنيف الافتراضي للمركبة عبر هذا المسار لا يميز `vehicle_oil` تلقائيًا؛ لا تغيّر ذلك في النقل دون فحص بقية المسارات المتخصصة.

# 3. لوحة التحكم المالية — FinancialDashboard

**اسم الملف:** `src/components/expenses/FinancialDashboard.tsx`  
**اسم المكوّن:** `FinancialDashboard`

## 3.1 ترتيب العناصر من أعلى لأسفل
1. رأس لوحة المالية.
2. اختيار الشهر.
3. زر إضافة مصروف.
4. مؤشرات/رسوم الدخل والمصروفات والفائض.
5. زر الانتقال إلى قائمة أقسام الدخل والمصروفات.
6. بطاقات الوصول السريع للأقسام.
7. سجل آخر المعاملات.

## 3.2 الرأس واختيار الشهر
- حقل الشهر: `input` في السطر المرجعي 130 وما حوله، يغيّر `selectedMonth` عبر `onSelectMonth`.
- تنسيق الشهر الظاهر عبر `formatMonthLabel(monthStr)`.
- يجب أن ينعكس الشهر المحدد على كل الإجماليات والبطاقات والسجل والتقارير التابعة له.
- عند الانتقال إلى شهر سابق/لاحق لا يتم إنشاء سجل تلقائي لمجرد العرض.

## 3.3 زر إضافة مصروف
- الموضع: أعلى لوحة التحكم قرب اختيار الشهر/رأس الإجراءات.
- handler: `onOpenAddExpense` (السطر المرجعي 137-138).
- الوظيفة: فتح `AddExpenseModal`.
- لا يحفظ بذاته؛ الحفظ يتم من النموذج بعد التحقق.

## 3.4 المؤشرات المالية
المعادلات المصدرية:
- `savingsRate = monthlyIncome > 0 ? clamp(round(netIncome/monthlyIncome*100),0,100) : 0`.
- `expenseRatio = monthlyIncome > 0 ? min(100, round(monthlyExpenses/monthlyIncome*100)) : 100`.
- `isSurplus = netIncome >= 0`.
تعرض نسبة المصروفات والفائض في مؤشرات/أشرطة رسومية، مع عناوين توضيحية على عناصر الرسم:
- `title="المصروفات: {expenseRatio}%"`.
- `title="الفائض: {savingsRate}%"`.
**تنبيه:** نسبة الفائض تُقص إلى المجال 0–100؛ إذا كان الصافي سالبًا ستظهر 0% بدل قيمة سالبة. لا تغيّر المعادلة أثناء النقل دون قرار منتج.

## 3.5 زر قائمة الأقسام
- handler: `onOpenSectionsMenu`، يظهر في موضعين ضمن اللوحة (مرجع السطرين 265-266 و295-296).
- الوظيفة: فتح شاشة `SectionsMenuScreen`.
- يجب أن تكون كلتا نقطتي الدخول إلى القائمة متاحتين إذا كانتا ظاهرتين في التصميم الفعلي.

## 3.6 بطاقات الوصول السريع
الأزرار/الوجهات المؤكدة:
- `handleNavigateSection('house')`: المنزل.
- `handleNavigateSection('personal')`: الشخصي.
- `handleNavigateSection('vehicle')`: المركبات.
- `handleNavigateSection('education')`: التعليم.
- `handleNavigateSection('income_certs')`: الدخل والشهادات.
- `handleNavigateSection('reports')`: التقارير.
- الدالة تسمح أيضًا بـ `work`، ويجب فحص موضع بطاقة العمل في JSX الكامل؛ لا تفترض عدم وجودها من قائمة نتائج مختصرة.
كل بطاقة تعرض أيقونة وعنوانًا/وصفًا ومبلغ القسم أو مؤشره، والنقر ينقل إلى القسم المقابل.

## 3.7 سجل أحدث المعاملات
- المصدر: `recentTransactions` المحسوب في `ExpensesView` من `allExpenses` الموحد.
- يعرض آخر 10 حركات بعد ترتيب التاريخ تنازليًا.
- الحقول المعروضة: اسم/عنوان المعاملة، شارة القسم، التاريخ، المبلغ.
- أنواع الشارات:
  - `house` منزل
  - `medical` طبي
  - `work` عمل
  - `personal` شخصي
  - `vehicle_fuel` وقود
  - `vehicle_maint` صيانة
  - `vehicle_oil` زيوت
  - `student` تعليم
- النقر على الحركة ينقل للقسم المرتبط بها.
- **مهم:** مصدر هذا السجل هو `finance_expenses` عبر `allExpenses` وليس قائمة قديمة محلية مستقلة.

# 4. قائمة أقسام الدخل والمصروفات — SectionsMenuScreen

**المكوّن:** `SectionsMenuScreen`  
**العنوان الظاهر:** «أقسام الدخل والمصروفات».

## 4.1 رأس الشاشة
- زر رجوع: أقصى طرف الرأس (يتغير اتجاه السهم حسب اللغة)، `onBack`.
- عنوان في منتصف الرأس مع أيقونة `Layers`.
- زر «ترتيب» في الطرف المقابل، handler يبدل `isReorderMode`.
- عند تفعيل الترتيب يظهر زر أيقونة `RotateCcw` لاستعادة الترتيب الافتراضي؛ handler `handleResetOrder`.

## 4.2 لوحة بيانات الدخل
بطاقة كاملة أسفل الرأس بعنوان «بيانات الدخل»، وبداخلها ثلاثة أعمدة:
- «الدخل»: `monthlyIncome` باللون الأخضر مع علامة +.
- «المصروفات»: `monthlyExpenses` باللون الأحمر مع علامة -.
- «الصافي»: `netIncome` وإشارة موجبة/سالبة ولون حسب الفائض/العجز.
هذه ليست أرقامًا مستقلة؛ يجب أن تأتي من نفس مصدر حساب لوحة التحكم.

## 4.3 وضع الترتيب
- عند تفعيل «ترتيب» تظهر رسالة مساعدة: «استخدم أزرار الأسهم أو اسحب البطاقات لتغيير ترتيبها».
- زر «حفظ والانتهاء ✓» يغلق وضع الترتيب.
- كل بطاقة تصبح قابلة للسحب وإعادة الإفلات.
- سهم لأعلى: `handleMove(index,'up')`.
- سهم لأسفل: `handleMove(index,'down')`.
- السهم الأول يتعطل لأول بطاقة، والسهم الثاني يتعطل لآخر بطاقة.
- زر الاستعادة يعيد `DEFAULT_SECTIONS_ORDER`.
- التخزين في `STORAGE_KEYS.EXPENSES_SECTIONS_ORDER` عبر localStorage في المصدر.
- عند النقل يجب حفظ الترتيب في DataStore/Room user preferences، لا في متغير مؤقت.

## 4.4 بطاقات الأقسام
- تظهر في شبكة عمود واحد للهاتف وعمودين للشاشات الأوسع.
- كل بطاقة تعرض: أيقونة، اسم القسم، وصفًا قصيرًا، إجمالي القسم، وعدد «عملية/سجل» ما لم تكن `noMoney`.
- النقر على البطاقة ينفذ `onSelectSection(sec.id)` ما لم يكن وضع الترتيب فعالًا.
- يجب نقل ترتيب `DEFAULT_SECTIONS_ORDER` كما هو من المصدر؛ لا تعتمد ترتيبًا جديدًا من هذه الوثيقة وحدها.

# 5. نموذج إضافة مصروف موحد — AddExpenseModal

**المكوّن:** `AddExpenseModal`  
**الملف:** `src/components/expenses/AddExpenseModal.tsx`  
**الحالة:** `section, category, amount, date, paymentMethod, notes`.

## 5.1 شكل النافذة
- نافذة Modal فوق الشاشة، عنوانها في الرأس.
- زر X/إغلاق في الرأس: `onClose`.
- جسم النموذج قابل للتمرير رأسيًا.
- أزرار الحفظ والإلغاء في أسفل النموذج.

## 5.2 ترتيب الحقول
1. **القسم**: مجموعة أزرار/بطاقات اختيار القسم من `sectionOptions` (السطر المرجعي 51 وما بعده). الأنواع المتاحة: `house, work, personal, vehicle, education`. عند التغيير يستدعى `handleSectionChange(sec)) ويعاد ضبط التصنيف المناسب للقسم.
2. **التصنيف**: قائمة `select` تتغير خياراتها من `categoryMap[section]`. القيمة الابتدائية `فطار` قبل ضبط القسم/الاختيارات، فلا تعتمدها كقيمة عامة لكل قسم.
3. **المبلغ**: حقل رقمي `type=number`، `min=0.01`، `step=0.01`، placeholder `0.00`. يجب أن يكون أكبر من صفر، ويُرفض النص غير الرقمي.
4. **التاريخ**: حقل تاريخ `type=date`، افتراضيه تاريخ اليوم بصيغة ISO `YYYY-MM-DD`.
5. **طريقة الدفع**: قائمة اختيار بالقيم الحرفية:
   - `نقدي (كاش)`
   - `بطاقة فيزا / ماستركارد` (العرض العربي «بطاقة بنكية / فيزا / ماستركارد»)
   - `تحويل بنكي / انستاباي`
   - `محفظة إلكترونية (فودافون كاش / وغيرها)`
   - `شيك / آجل`
   الافتراضي `نقدي (كاش)`.
6. **ملاحظات**: حقل نصي اختياري، placeholder «تفاصيل إضافية عن المصروف...».
7. **إلغاء**: زر ينفذ `onClose` ولا يحفظ.
8. **حفظ المصروف**: زر submit؛ ينفذ `handleSubmit`، يتحقق من المبلغ ثم يستدعي `onSave`/المسار الموحد.

## 5.3 منطق الإضافة
- `handleSubmit` يمنع submit الافتراضي.
- يحول المبلغ إلى رقم.
- يجب ألا يسمح بحفظ مبلغ غير صالح/صفر/سالب.
- يرسل القسم والتصنيف والمبلغ والتاريخ وطريقة الدفع والملاحظات إلى الحاوية.
- الحاوية تحول القسم إلى canonical type ثم POST إلى API.
- عند نجاح الخادم: إعادة تحميل السجل الموحد وإغلاق النافذة.
- عند فشل الخادم: إظهار خطأ قابل للفهم، إبقاء البيانات المدخلة كي لا يفقدها المستخدم.

## 5.4 متطلبات النقل
- يجب أن تكون قائمة التصنيفات ديناميكية حسب القسم مثل المصدر.
- حفظ طريقة الدفع كقيمة معيارية واضحة، مع إبقاء النص الأصلي للعرض عند الحاجة.
- لا تنشئ سجلًا محليًا وسجلًا خادميًا منفصلين لنفس العملية.
- يجب دعم منع الضغط المزدوج أثناء الحفظ.

# 6. أنواع المصروفات الموحدة Canonical Finance

## 6.1 عقد النوع
من `src/types/finance.ts`:
`ExpenseType = house | medical | personal | student | vehicle_fuel | vehicle_maint | vehicle_oil | work`.

من `src/contracts/financeContract.ts`:
`Transaction`:
- `id: string`
- `type: ExpenseType`
- `amount: number`
- `date: string`
- `categoryId: string`
- `note: string`
- `source: string`
- `createdAt: string`
- `updatedAt: string`

ومن `src/types/finance.ts`:
- `note: string | null`
- `source: FinanceRecordSource`، والقيم: `legacy_storage | finance_sqlite | smart_ai | migration | manual | import`.

**فرق مهم:** هناك تعريفان للعقد في ملفين، مع اختلاف nullable/source/تسمية حقول التقرير؛ يجب حسم DTO واحد في Kotlin يعكس API الفعلي، لا دمج التعريفين عشوائيًا.

## 6.2 أنواع المصروفات الثمانية
| type | المعنى | legacy key |
|---|---|---|
| house | المنزل | `smart_time_house_expenses` |
| medical | طبي | `smart_time_medical_expenses` |
| personal | شخصي | `smart_time_personal_expenses` |
| student | طالب/تعليم | `smart_time_student_expenses` |
| vehicle_fuel | وقود | `smart_time_vehicle_fuel` |
| vehicle_maint | صيانة | `smart_time_vehicle_maint` |
| vehicle_oil | زيوت/فلاتر | `smart_time_vehicle_oil_filters` |
| work | عمل | `smart_time_work_expenses` |

لا تعتبر `vehicle_oil` مرادفًا لـ `vehicle_maint`؛ كلاهما نوع مستقل في التقارير الموحدة.

## 6.3 DTO الإضافة من Finance API
`CanonicalExpenseInput`:
- `id?`
- `type` إلزامي
- `title?`
- `amount` إلزامي
- `category?`
- `date` إلزامي
- `paymentMethod?`
- `receiptUrl?`
- `notes?: string | null`
- `createdAt?`

قواعد الافتراضي في `createCanonicalFinanceExpense`:
- title = title أو category أو type.
- category = category أو type.
- paymentMethod = paymentMethod أو `unknown`.
- POST إلى `/api/finance/expenses`.

عمليات أخرى:
- GET `/api/finance/overview`.
- PATCH `/api/finance/expenses/{id}`.
- DELETE `/api/finance/expenses/{id}`.
- POST `/api/finance/migrate` للمصالحة/الترحيل.
كل الطلبات تستخدم credentials include في الويب؛ في Kotlin يجب نقل آلية الجلسة المكافئة (Cookie/Token) حسب الخادم الفعلي، لا افتراض أن include موجودة تلقائيًا.

# 7. شاشة الدخل والشهادات والحساب الجاري

**المكوّن:** `IncomeAndCertificatesSection`  
**الملف:** `src/components/expenses/IncomeAndCertificatesSection.tsx`  
**حالة التبويب:** `activeTab = menu | income | certificates | all` (يجب التحقق من كل الفروع المستخدمة فعليًا).  
**تبويبات فرعية:** `incomeMode = job | free | car`، و `bankTab = certificates | current`.

## 7.1 رأس الشاشة والتنقل
- زر رجوع في أعلى الشاشة: إذا `activeTab === 'menu'` ينفذ `onBack()`، وإلا يعيد `activeTab` إلى `menu`.
- شاشة القائمة تعرض زرين متجاورين:
  1. «الدخل الشهري» → `setActiveTab('income')`.
  2. «الشهادات البنكية/المعاملات البنكية» → `setActiveTab('certificates')`.
- كما تعرض بطاقتين كبيرتين في القائمة لنفس الوجهتين، مع وصف/إجماليات؛ الضغط يفتح التبويب.

## 7.2 تبويب الدخل الشهري
في أعلى المحتوى شريط تبويب:
- «الدخل الشهري» (نشط أخضر).
- «معاملات بنكية» (ينقل إلى تبويب البنك).

أسفله اختيار نوع الدخل:
- «الوظيفة» → `incomeMode='job'`.
- «دخل حر» → `incomeMode='free'`.
- «دخل سيارة 🚗» → `incomeMode='car'` ويعرض `CarIncomeSection` مباشرة.
عند اختيار «دخل سيارة» لا تعرض نموذج الدخل العام؛ تظهر شاشة دخل السيارة في نفس المسار.

### 7.2.1 وضع الوظيفة
شريط فرعي من أربعة أزرار:
- «الراتب الشهري» `salary`.
- «مكافآت» `bonus`.
- «انتقالات» `transfer`.
- «أخرى» `other`.
يحدد الزر نوع السجل الجاري إدخاله ويغيّر placeholder الوصف.

### 7.2.2 وضع الدخل الحر
- لا يظهر شريط أنواع الوظيفة.
- placeholder الوصف: «مثال: عمل حر / مشروع».
- النوع المحفوظ فعليًا يصبح `extra` عند `incomeMode === 'free'`.

### 7.2.3 نموذج الإدخال الظاهر في المحتوى
صف من عمودين (وعلى الهاتف عمود واحد):
- «الوصف» / `sourceName`: نص؛ أمثلة placeholder حسب النوع: «مثال: الراتب الأساسي»، «مثال: مكافأة الأداء»، «مثال: بدل انتقالات»، «مثال: دخل آخر»، أو «مثال: عمل حر / مشروع».
- «القيمة» / `sourceAmount`: رقم، min=0، step=0.01، placeholder `0`.
- شريط «الإجمالي المحسوب تلقائياً»: يعرض مجموع مصادر الشهر الحالية زائد قيمة الحقل الجاري إدخالها. لا تخلط هذا الرقم بإجمالي دخل النظام الذي يشمل الشهادات والحساب الجاري والجمعيات.
- زر «حفظ الدخل» أو «حفظ التعديل» حسب `editingSourceId`. ينفذ `handleSaveSource`.

**ملاحظة مهمة:** متغيرات الحالة تشمل `sourceDate`, `sourceType`, `sourceNotes` لكن المقطع الظاهر من النموذج لا يعرض حقول التاريخ والنوع والملاحظات في هذا الموضع؛ بعض الإضافة قد تتم من Modal منفصل أو عبر مسار حالة تحرير. لا تضف حقولًا إلى نفس الشاشة وتزعم أنها ظاهرة هنا دون مراجعة JSX الكامل لجميع الفروع.

### 7.2.4 قائمة الدخل المحفوظ
بطاقة أسفل نموذج الإدخال:
- العنوان «الدخل المحفوظ».
- إجمالي المصادر `primaryIncomeTotal`.
- زر «تصدير» → `exportIncomeData('csv')`.
- زر طباعة بأيقونة `Printer` وtitle «طباعة / PDF» → `exportIncomeData('print')`.
- كل صف يعرض اسم المصدر، النوع، التاريخ، القيمة بعلامة +.
- زر تعديل `Edit2`: يحمل ID والاسم والمبلغ والتاريخ والنوع والملاحظات إلى state التحرير، ويحدد وضع job/free والتبويب الفرعي المناسب.
- زر حذف `Trash2`: `handleDeleteSource(id)` ويعيد حساب مجموع salary/bonus/other ويحفظ الشهر.
- حالة فارغة: «لم يتم حفظ أي دخل بعد».

### 7.2.5 أنواع مصادر الدخل المحفوظة
من قائمة select في نافذة إضافة المصدر:
- `salary`: راتب أساسي.
- `extra`: دخل إضافي / عمل حر.
- `bonus`: مكافآت وحوافز.
- `investment`: أرباح استثمار وتجارة.
- `other`: أخرى.
كما يظهر في تبويبات الوظيفة نوع `transfer` للانتقالات.
**لا تدمج transfer مع bonus ولا تحذف investment؛ احتفظ بالقيم كما هي في DTO.**

## 7.3 تبويب المعاملات البنكية
في أعلى التبويب:
- زر «الدخل الشهري» للعودة.
- زر «معاملات بنكية» نشط.
ثم شريط داخلي:
- «الشهادات البنكية» → `bankTab='certificates'`.
- «الحساب الجاري» → `bankTab='current'`.

## 7.4 الشهادات البنكية
### 7.4.1 زر الإضافة
- زر عريض أعلى قائمة الشهادات.
- النص الظاهر «إضافة معاملة +» (بالإنجليزية Add Transaction +).
- أيقونة `Building`.
- handler يصفّر حالة النموذج:
  - editingCertId = null
  - certBankName = ''
  - certNumber = ''
  - certAmount = ''
  - certAnnualRate = '18'
  - certDuration = '1 سنة'
  - certFrequency = 'monthly'
  - certNotes = ''
  - ثم `setIsCertModalOpen(true)`.
- هذا الزر يفتح نموذج شهادة بنكية، رغم تسمية الزر «إضافة معاملة».

### 7.4.2 ملخص الشهادات
بطاقة أفقية:
- «إجمالي أصل المعاملات البنكية المحفوظة»: مجموع `certificates.amount`.
- «العائد الشهري المحسوب»: `monthlyCertsProfit`.
- العملة بجوار القيم.
هذه الأرقام منفصلة: الأصل ليس دخلًا شهريًا؛ العائد هو ما يدخل الحساب الشهري بحسب قواعد الدالة.

### 7.4.3 بطاقات الشهادات
- شبكة بطاقة/عمودين على الشاشة الواسعة وعمود واحد للهاتف.
- كل بطاقة تعرض اسم البنك `cert.bankName` ورقم الشهادة إذا وجد.
- في أعلى البطاقة يمين/يسار: زر تعديل `Edit2` → `openEditCert(cert)`، وزر حذف `Trash2` → `handleDeleteCert(cert.id)`.
- أسفل بيانات البنك: قيمة الأصل `cert.amount`.
- العائد الشهري المعروض `calculateCertificateProfits(...).monthlyEquivalent`.
- حالة فارغة: «لا توجد معاملات بنكية مضافة بعد».

### 7.4.4 حقول نموذج الشهادة
تفتح نافذة Modal. الحقول المؤكدة من حالات النموذج والـJSX:
1. اسم البنك `certBankName` — label «اسم البنك *»، نص، placeholder «مثال: البنك الأهلي، بنك مصر»، مطلوب.
2. رقم الشهادة `certNumber` — label «رقم الشهادة (اختياري)»، نص، placeholder `CERT-10293`.
3. قيمة الشهادة/الأصل `certAmount` — label «قيمة الشهادة (الأصل) *»، رقم، placeholder مثال `5000`، مطلوب.
4. نسبة العائد السنوية `certAnnualRate` — label «نسبة العائد السنوية % *»، رقم، default `18`، placeholder `18`، مطلوب.
5. مدة الشهادة `certDuration` — label «مدة الشهادة *»، اختيار، default «1 سنة». خيارات القائمة يجب نقلها حرفيًا من JSX عند تنفيذ الواجهة.
6. دورية العائد `certFrequency` — قيمها في الحساب: `monthly | quarterly | semiannual | annual | maturity`. انقل labels والخيارات الحرفية من JSX الكامل.
7. تاريخ الإصدار `certIssueDate` — تاريخ، افتراضي اليوم.
8. تاريخ الاستحقاق `certMaturityDate` — تاريخ محسوب افتراضيًا اعتمادًا على المدة وتاريخ الإصدار؛ عند تغيير المدة يعاد حسابه عبر `handleDurationChange(val)`.
9. تاريخ نزول العائد `certProfitDate` — تاريخ، افتراضي اليوم.
10. ملاحظات `certNotes` — نص اختياري.
11. أزرار النموذج: حفظ/تعديل وإغلاق/إلغاء؛ تحقق من بقية JSX للحصول على النصوص الدقيقة وأسماء الـhandlers.
الـstates والـhandlers: `handleSaveCertificate`, `handleDeleteCert`, `openEditCert`, `handleDurationChange`.

### 7.4.5 الحفظ والتحقق
- تحويل قيمة الأصل `parseFloat(certAmount)`.
- تحويل المعدل `parseFloat(certAnnualRate)`.
- حساب الأرباح عبر `calculateCertificateProfits(amount, rate, frequency, duration)`.
- إنشاء `BankCertificate` جديد يحتوي بيانات البنك والرقم والأصل والمعدل والمدة وتواريخ الإصدار والاستحقاق/نزول العائد والتواتر والملاحظات وحقول الأرباح المحسوبة.
- إذا كان تعديلًا، يستبدل السجل المطابق؛ وإلا يضاف سجل جديد.
- الحذف يفلتر السجل بالـID ويحفظ القائمة.
- فتح التعديل يملأ جميع الحقول من الشهادة الموجودة.
- يجب أن يطلب تأكيد حذف في Kotlin إذا كان ذلك سلوك المصدر/التصميم المعتمد؛ لا تضف حذفًا صامتًا.

## 7.5 الحساب الجاري
### 7.5.1 بطاقة الرصيد
- العنوان «قيمة الحساب الجاري الحالية».
- الرصيد `currentAccountBalance`، أخضر إذا غير سالب وأحمر إذا سالب.
- النص التوضيحي: «تضاف القيمة الصافية تلقائياً إلى إجمالي الدخل الشهري».
- الرصيد = مجموع مبالغ معاملات الحساب الجاري في الشهر المحدد، والإيداع موجب والسحب سالب.

### 7.5.2 اختيار نوع الحركة
زر «إيداع» → `currentAccountMode='deposit'` أخضر.
زر «سحب» → `currentAccountMode='withdrawal'` أحمر.
لا تضع إشارة سالبة يدويًا في حقل السحب؛ النموذج يأخذ قيمة موجبة ثم يحولها handler إلى قيمة سالبة.

### 7.5.3 حقول نموذج الحركة
- «الوصف» `currentAccountDescription`: مطلوب، placeholder «مثال: تحويل من البنك».
- «قيمة الإيداع» `currentAccountAmount`: رقم، min=0.01، step=0.01، مطلوب، placeholder `0`. يظهر النص نفسه حتى عند اختيار السحب في المصدر.
- «التاريخ» `currentAccountDate`: تاريخ مطلوب، default أول يوم في الشهر المحدد.
- زر submit: «حفظ العملية» أو «حفظ التعديل» حسب `editingCurrentAccountId`.
- handler: `saveCurrentAccount`.
- يحول المبلغ إلى رقم موجب، ثم يطبق:
  - deposit: `+value`
  - withdrawal: `-value`
- نوع السجل: `current_deposit` أو `current_withdrawal`.
- يدعم إضافة وتعديل وحفظ ضمن `MonthlyIncome.sources`.

### 7.5.4 قائمة عمليات الحساب الجاري
- رأس البطاقة: «عمليات الحساب الجاري» + الرصيد الحالي.
- زر «تصدير» → `exportCurrentAccount('csv')`.
- زر طباعة → `exportCurrentAccount('print')`.
- كل صف يعرض الوصف، نوع «سحب/إيداع»، التاريخ، المبلغ بعلامة مناسبة.
- زر تعديل: يعيد تعبئة الوصف والمبلغ المطلق والتاريخ والنوع.
- زر حذف: `deleteCurrentAccount(id)`.
- حالة فارغة: «لا توجد عمليات محفوظة.»
- CSV headers: النوع، الوصف، القيمة، التاريخ؛ اسم الملف `current-account-{selectedMonth}.csv`.

# 8. قواعد حساب الدخل والشهادات

## 8.1 الدخل الأساسي
الدالة: `getPrimaryIncomeForMonth(incomes, targetMonth)`.
- تبحث عن مستند `MonthlyIncome` حيث `month === targetMonth`.
- إذا لم يوجد: total/salary/bonuses/other/sourcesCount = 0.
- إذا كان `sources` غير فارغ:
  - تستبعد `current_deposit` و`current_withdrawal` من الدخل المكتسب.
  - تجمع بقية المصادر.
  - salary هو المصادر التي type=salary أو source يحتوي «راتب».
  - other = total - salary.
  - bonuses تعرض قيمة `currentIncomeDoc.bonuses` أو 0.
- إذا لم توجد sources:
  - total = salary + bonuses + otherIncome.
- يجب الانتباه إلى أن `other` قد يتضمن bonus/transfer/extra/investment/other في مسار sources، فلا تجمعه مرة ثانية فوق total.

## 8.2 أرباح الشهادات
الدوال:
- `extractDurationYears(duration)`
- `calculateCertificateProfits(amount, annualRate, frequency, durationStr)`
- `getCertificatesProfitForMonth(certificates, targetMonth)`

الحساب:
- annualProfit = amount × annualRate / 100.
- extractDurationYears يقرأ أول رقم؛ إذا النص يحتوي «شهر» أو month يقسم الرقم على 12، وإلا يعامل الرقم سنوات؛ إذا لم يستخرج رقمًا يرجع 1.
- monthly: periodicProfit = annualProfit/12.
- quarterly: periodicProfit = annualProfit/4، وmonthlyEquivalent = annualProfit/12.
- semiannual: periodicProfit = annualProfit/2، وmonthlyEquivalent = annualProfit/12.
- annual: periodicProfit = annualProfit، وmonthlyEquivalent = annualProfit/12.
- maturity: periodicProfit = annualProfit × years، وmonthlyEquivalent = (annualProfit × years)/(years × 12).
- التقريب إلى منزلتين عشريتين.
- إذا وجد `monthlyEquivalentProfit` موجبًا يستخدم أولًا.
- وإلا إذا وجد annualRate وamount موجبان يحسب من المعدل والدورية والمدة.
- وإلا يستخدم profitAmount ويحوّله إلى مكافئ شهري حسب الدورية.
- الشهادة تُحسب ضمن الشهر فقط إذا كان `issueMonth <= targetMonth <= maturityMonth` عند وجود تاريخين صالحين. إذا التواريخ ليست بصيغة صحيحة، الكود الحالي يسمح بالإدراج افتراضيًا.
**تنبيه دقيق:** `getCertificatesProfitForMonth` يستخدم monthlyEquivalent كدخل شهري موزع، وليس تاريخ نزول الربح الفعلي في كل شهر ربع سنوي/سنوي. لذلك لا تكتب في Kotlin أنها تصرف فعليًا في تاريخ نزول الربح دون تغيير واعتماد قاعدة العمل. وثّق هذا الفرق واحتفظ بسلوك React إلى أن يعتمد المستخدم قرارًا جديدًا.

## 8.3 الحساب الجاري
- `currentAccountNet` يجمع المصادر ذات النوع current_deposit/current_withdrawal في مستند الشهر المحدد.
- الإيداع موجب، السحب سالب.
- لا يُضاف الحساب الجاري إلى `primaryIncome.total` داخل الدالة؛ يضاف في مستوى ExpensesView مرة واحدة.
- يجب منع ازدواج الجمع.

## 8.4 الجمعيات
من `ExpensesView`:
- لكل جمعية، إذا كان تاريخ البدء قبل/عند أول الشهر المحدد، يحسب monthIndex.
- إذا `0 <= monthIndex < max(1,floor(memberCount))`، يضاف القسط إلى `installments`.
- إذا كان شهر `payoutDate` هو الشهر المحدد، يضاف `value` إلى `earned`.
- `associationFinancials.installments` تحسب لكنها لا تدخل في monthlyIncome في المعادلة الحالية المعروضة.
- `associationFinancials.earned` تدخل في الدخل الشهري.
- لا تضف الأقساط إلى المصروفات الموحدة تلقائيًا إلا وفق التقارير/سلوك المصدر؛ التقرير قد يبني صفوفًا تفصيلية مستقلة لها.

## 8.5 إجمالي الدخل والصافي
المعادلة الفعلية في `ExpensesView`:
`monthlyIncome = primaryIncome.total + certificatesProfit + currentAccountNet + associationFinancials.earned`.
`monthlyExpenses = house + medical + work + personal + fuel + maintenance + oil + education`.
`netIncome = monthlyIncome - monthlyExpenses`.

## 8.6 إجمالي المصروفات
- يتم ترشيح `allExpenses` حسب `date.slice(0,7) === selectedMonth`.
- تجمع الأنواع الثمانية canonical.
- `vehicleTotal = fuelTotal + maintTotal + oilTotal`.
- `homeAndMedicalTotal = houseTotal + medicalTotal`.
- الحوادث تُرشح للشهر لكنها ليست ضمن معادلة `monthlyExpenses` في الكود المعروض.
- لا تجمع إجمالي السيارة مرة أخرى بجانب fuel/maint/oil؛ vehicleTotal مجرد تجميع فرعي.

# 9. دخل مشاوير السيارة — CarIncomeSection

**الملف:** `src/components/expenses/CarIncomeSection.tsx`  
**المكوّن:** `CarIncomeSection`.

## 9.1 التبويبات
- «إدخال» `activeSubTab='entry'`.
- «تقرير» `activeSubTab='report'`.
التنقل يتم بأزرار في أعلى القسم.

## 9.2 نموذج تسجيل المشوار
المصدر يحتوي:
- نموذج `onSubmit={handleSaveTrip}`.
- حقل سعر الرحلة `tripAmount`، رقم، placeholder `320`.
- حقل اسم/وصف الرحلة `tripName`، placeholder «مثال: رحلة 1، مشوار التجمع».
- أزرار مبالغ سريعة `amt` تنفذ `setTripAmount(String(amt))`.
- أزرار أنواع رحلة/تطبيق `type.label` تضبط `tripType`.
- حقل الملاحظات `tripNotes`، placeholder «مثال: من المعادي إلى مصر الجديدة، انتظار 15 دقيقة».
- زر `setToCurrentDateTime` لضبط الوقت والتاريخ الحالي، title «ضبط على الوقت والتاريخ الحالي».
- حقول التاريخ والوقت (موجودة حول الأسطر 734 و747؛ يجب نقل أسماء labels والـstate الدقيقة من كامل JSX).
- زر إلغاء التعديل `cancelEdit`.
- زر حفظ النموذج `handleSaveTrip`.
- قيم الإدخال الافتراضية والحقول الإضافية (رقم الرحلة/الوقت/التاريخ/نوع التطبيق) يجب مطابقتها من الملف الأصلي كاملًا.

## 9.3 قائمة الرحلات
- كل صف/بطاقة يعرض بيانات المشوار.
- زر تعديل `startEdit(t)`، أيقونة `Edit2`، title «تعديل».
- زر حذف `handleDeleteTrip(t.id)`، أيقونة `Trash2`، title «حذف».
- يدعم البحث بالرقم أو الملاحظات، حقل placeholder «بحث بالرقم أو الملاحظات...».
- مرشح الفترة عبر `setReportFilterPeriod(p.id)`.
- اختيار شهر/فترة عبر `select` في التقرير.
- إحصاءات التقرير: إجمالي الدخل، عدد الرحلات، متوسط الرحلة.
- التقرير يعرض أعمدة: م، رقم الرحلة، اليوم، التاريخ، الوقت، سعر الرحلة، نوع الرحلة/التطبيق، ملاحظات.

## 9.4 أزرار تصدير تقرير السيارة
- Excel: `handleExportExcel`, title «تصدير إكسيل Excel».
- Word: `handleExportWord`, title «تصدير وورد Word».
- طباعة/PDF: `window.print()`, title «طباعة ومعاينة PDF».
- نسخ ملخص نصي: `handleCopyTextReport`.
- كل زر له إخراج منفصل؛ لا تكتفِ بزر مشاركة عام.

# 10. التقارير المالية العامة — FinancialReportsSection

**المكوّن:** `FinancialReportsSection` (المصدر مضغوط في JSX في بعض الأجزاء).

## 10.1 رأس الشاشة
- زر رجوع `onBack`.
- أيقونة `BarChart3`.
- العنوان «تقاريري».
- الوصف «اختر القسم والفترة ونوع التقرير ثم عاين التفاصيل قبل التصدير».

## 10.2 فلاتر الفترة
- «من تاريخ» `fromDate`، حقل تاريخ، default أول يناير للسنة الحالية.
- «إلى تاريخ» `toDate`، حقل تاريخ، default اليوم، وmin=fromDate.
- «نوع التقرير» select:
  - `day`: تقرير يومى.
  - `week`: تقرير أسبوعى.
  - `month`: تقرير شهرى.
- عند اختيار day يظهر «اختر اليوم داخل الفترة» بقائمة أيام الفترة.

## 10.3 اختيار القسم
بطاقات اختيار القسم، وتظهر لكل بطاقة أيقونة واسم ووصف:
- كل الأقسام.
- المنزل.
- العمل.
- الشخصي.
- المركبات.
- التعليم.
- الطبي.
- الجمعيات.
- الوقود/الصيانة/الزيوت وفق البطاقات التي يعرفها المصدر.
- الدخل عند توفره في إعداد cards.
تغيير البطاقة يغير `section` المستخدمة في ترشيح صفوف التقرير.

## 10.4 معاينة التقرير
- زر عريض «معاينة التقرير بالتفصيل» مع أيقونة Eye.
- يفتح Modal.
- رأس المعاينة يعرض نوع التقرير واسم القسم والفترة وعدد الحركات.
- زر X يغلق `previewOpen`.
- الجدول يعرض: التاريخ، القسم، البيان، المبلغ، التفاصيل.
- المبالغ الموجبة خضراء والسالبة حمراء.
- الحالة الفارغة: «لا توجد حركات تفصيلية في الفترة المحددة».
- أسفل المعاينة يظهر إجمالي الحركات.
- أزرار التصدير:
  - Excel → `csv()`، مع أيقونة FileSpreadsheet ونص Excel.
  - Word → `word()`، مع أيقونة FileText.
  - طباعة/PDF → `window.print()`.

## 10.5 صفوف التقارير وقواعد التوليد
- التقرير يجمع من canonical expenses ومن القوائم المتخصصة التي تمررها ExpensesView (المنزل/الطبي/الشخصي/الجمعيات/الوقود/الصيانة/الزيوت/الحوادث/مصروفات الطلاب/الدخل).
- صف التقرير له: `date, section, item, amount, details`.
- الدالة `add(date,sectionName,item,value,details)` لا تضيف الصف إلا إذا كان التاريخ داخل الفترة.
- للجمعيات، `assocRows` يولد أقساطًا سالبة على تواريخها وقبض الجمعية موجبًا في payoutDate؛ لا تخلط بين «قسط الجمعية» و«قبض الجمعية».
- عنوان التقرير: يومي/أسبوعي/شهري.
- CSV/Word/PDF يجب أن يستخدم نفس مجموعة `rows` بعد الفلاتر؛ لا تحسب كل صيغة بطريقة مختلفة.

# 11. أقسام المصروفات التفصيلية

> ملفات الأقسام التالية كبيرة وتحتوي نماذج وسجلات تخصصية. ما يلي يحدد مسؤولية كل شاشة وما يجب أن يُحفظ في السجل الموحد. للحصول على كل نصوص الأزرار والحقول في كل modal يجب إكمال مسح JSX كاملًا سطرًا بسطر، ولا يجوز الادعاء أن هذه الفقرة تسرد كل label في الملفات.

## 11.1 المنزل — HouseExpensesSection
- ملف: `src/components/expenses/HouseExpensesSection.tsx` (650 سطرًا).
- مسؤول عن المصروفات المنزلية وسجلاتها وتفاصيلها.
- يمرر من الحاوية `houseExpenses` وبيانات طبية/منزلية حسب العقد.
- كل مصروف منزل canonical type = `house`.
- يجب الحفاظ على التصنيفات الفرعية والمرفقات/الملاحظات والعمليات التي في JSX.
- لا تدمج الطبي داخل house في السجل canonical؛ الطبي نوع مستقل `medical` وإن كان يظهر في إجمالي «المنزل والطبي» في بعض البطاقات.

## 11.2 الشخصي — PersonalExpensesSection
- ملف: `src/components/expenses/PersonalExpensesSection.tsx` (ملف صغير نسبيًا 41 سطرًا لكنه يمرر/يعرض واجهة متخصصة).
- type = `personal`.
- راجع props والمكوّن الذي يستدعيه قبل نقل النموذج؛ لا تفترض أن الـ41 سطرًا هي كل واجهة إدخال المصروف الشخصي.

## 11.3 العمل — WorkExpensesSection
- ملف: `src/components/expenses/WorkExpensesSection.tsx` (450 سطرًا).
- type = `work`.
- يحتفظ بمصروفات الأعمال ويجب أن يدعم التعديل والحذف والتصنيف والتاريخ والمبلغ والملاحظات.
- يوجد في نظام أنواع المصروفات المتخصصة `SpecializedExpenseItem` حقلا `paymentType: supply | labor` و`customType`، ويجب عدم إسقاط تفاصيل المواد/العمالة عند نقل المصروفات التي تستخدم هذا النموذج.

## 11.4 المركبات — VehicleExpensesSection
- ملف: `src/components/expenses/VehicleExpensesSection.tsx` (615 سطرًا).
- يضم مصروفات وقود وصيانة وزيوت/فلاتر، مع سجلات تفصيلية.
- الوقود type = `vehicle_fuel`.
- الصيانة type = `vehicle_maint`.
- الزيت/الفلاتر type = `vehicle_oil`.
- يجب الحفاظ على حقول القراءة بالكيلومتر/المحطة/التكلفة/النوع/الملاحظات حسب مصدر السجل.
- لا تسجل تكلفة الوقود/الصيانة مرتين: مرة في سجل المركبة ومرة كـcanonical expense منفصل دون ربط ID أو قاعدة منع تكرار.

## 11.5 التعليم — EducationExpensesSection
- ملف: `src/components/expenses/EducationExpensesSection.tsx` (131 سطرًا).
- type = `student`.
- ربط المصروف بالطالب/المادة/الدرس حيث يتوفر.
- يجب الاحتفاظ ببيانات الطالب وسبب المصروف لا مجرد المبلغ.
- راجع `StudentExpenseRecord`: `id, studentId, subCategory(lessons|personal|transport|school|books), title, amount, date, notes?`.

# 12. نماذج البيانات المالية الإضافية

## 12.1 MonthlyIncome
في المصدر: `MonthlyIncome` مع:
- `id`
- `month` بصيغة YYYY-MM
- `salary`
- `bonuses`
- `otherIncome`
- `sources: IncomeSourceItem[]`
- `createdAt`
- `updatedAt`
يجب الحفاظ على المصادر التفصيلية وعدم الاكتفاء بثلاثة أرقام مجمعة.

## 12.2 IncomeSourceItem
من استخدام المكوّن:
- `id`
- `source` اسم/وصف المصدر
- `amount`
- `date`
- `type`: salary / bonus / transfer / other / extra / investment / current_deposit / current_withdrawal
- `notes?`
تُستخدم نفس collection للمصادر المكتسبة وحركات الحساب الجاري، لكن الدوال تستبعد حركات الحساب الجاري عند حساب earned income.

## 12.3 BankCertificate
الحقول التي يعتمد عليها JSX والحساب:
- `id`
- `bankName`
- `certificateNumber?`
- `amount`
- `annualRate?`
- `duration`
- `issueDate`
- `maturityDate`
- `profitDate`
- `profitFrequency`
- `profitAmount?`
- `monthlyEquivalentProfit?`
- `notes?`
- حقول الإنشاء/التعديل إذا كانت موجودة في type.
**يجب قراءة تعريف BankCertificate النهائي من src/types.ts قبل تنفيذ DTO Kotlin.**

## 12.4 AssociationRecord
تُستخدم الحقول:
- `id`
- `name`
- `startDate`
- `memberCount`
- `installment`
- `payoutDate`
- `value`
- `roleNumber`
قد تختلف أسماء بعض الحقول في type النهائي؛ اعتمد تعريف TypeScript الأصلي.

## 12.5 Budget / Category / Reports
- `Category`: id,type,name,color,icon.
- `Budget`: id,type,month,limit,spent.
- `FinanceReports`: byType,byMonth,totals.
- `FinanceReportTotals`: expenses,income,balance.
- التقرير حسب النوع والشهر يحتوي total/count.

# 13. توحيد مصادر البيانات وقواعد عدم التكرار

## 13.1 المصدر النهائي للمصروفات
وفق `docs/ai/EXPENSE-PHASE5-FINAL-VERIFICATION.md`:
- `finance_expenses` هو دفتر المعاملات canonical النهائي.
- UI وSMART AI يقرآن الإسقاط نفسه.
- `ai_transactions` ليس مصدر كتابة ماليًا مستقلاً، بل توافق/مصالحة قديم.
- legacy localStorage مصدر ترحيل/توافق فقط، وليس مصدر كتابة دائمًا بعد التوحيد.

## 13.2 API المؤكد
من `src/services/financeService.ts`:
- GET `/api/finance/overview`
- POST `/api/finance/expenses`
- PATCH `/api/finance/expenses/:id`
- DELETE `/api/finance/expenses/:id`
- POST `/api/finance/migrate`

## 13.3 قواعد الترحيل
- الاحتفاظ بالـIDs إذا أمكن.
- منع التكرار حسب ID والمحتوى.
- إظهار التعارضات بدل حذفها.
- قراءة السجلات بعد الإدخال للتأكد.
- التراجع transaction rollback عند فشل الترحيل.
- عدم حذف مصادر legacy أثناء الترحيل.
- لا تكتب في `ai_transactions` من واجهة المصروفات.
- لا تجعل Kotlin يكتب في Room وفي API كسجلين مستقلين. استخدم Outbox/Mirror واضحًا عند الحاجة offline.

# 14. واجهة Kotlin المطلوبة شاشة بشاشة

يجب إنشاء/إكمال هذه الشاشات على الأقل:
1. `FinanceDashboardScreen`.
2. `FinanceSectionsMenuScreen`.
3. `AddExpenseSheet` / `AddExpenseDialog`.
4. `HouseExpensesScreen`.
5. `PersonalExpensesScreen`.
6. `WorkExpensesScreen`.
7. `VehicleExpensesScreen`.
8. `EducationExpensesScreen`.
9. `IncomeCertificatesScreen`.
10. `MonthlyIncomeScreen`.
11. `BankCertificatesScreen`.
12. `CurrentAccountScreen`.
13. `CarIncomeEntryScreen`.
14. `CarIncomeReportScreen`.
15. `FinancialReportsScreen`.
16. `ReportPreviewScreen`.
17. نماذج إضافة/تعديل مستقلة أو مشتركة حسب source behavior.

مكونات مشتركة:
- `MonthPicker`
- `MoneyAmountField`
- `DateField`
- `PaymentMethodPicker`
- `ExpenseCategoryPicker`
- `CurrencyAmountText`
- `FinanceSummaryCard`
- `EmptyState`
- `LoadingState`
- `ErrorState`
- `DeleteConfirmationDialog`
- `ExportActionMenu)

## 14.1 ViewModels / UseCases
- FinanceDashboardViewModel
- ExpenseListViewModel لكل قسم أو ViewModel موحد مع filter type
- AddExpenseViewModel
- IncomeCertificatesViewModel
- CarIncomeViewModel
- FinancialReportsViewModel

Use cases:
- GetFinanceOverview
- ObserveCanonicalExpenses
- CreateExpense
- UpdateExpense
- DeleteExpense
- CalculateMonthlyFinance
- SaveMonthlyIncomeSource
- DeleteMonthlyIncomeSource
- SaveBankCertificate
- CalculateCertificateReturn
- SaveCurrentAccountMovement
- DeleteCurrentAccountMovement
- SaveCarTripIncome
- BuildFinancialReport
- ExportFinanceReport

# 15. اختبارات القبول الإلزامية

## 15.1 اختبار إضافة مصروف
- أضف مصروف منزل 100 بتاريخ داخل الشهر.
- تحقق من ظهور 100 في المنزل، وإجمالي المصروفات، وآخر المعاملات، والتقرير.
- عدّل المبلغ إلى 125 وتأكد من استبدال القيمة لا إنشاء سجل مكرر.
- احذف السجل وتأكد من انعكاس الحذف في كل الشاشات.

## 15.2 اختبار النوع
- أنشئ مصروفًا لكل من الأنواع الثمانية.
- تأكد أن الوقود لا يصنف صيانة، والزيوت لا تدمج بالصيانة، والتعليم لا يظهر كشخصي.

## 15.3 اختبار الدخل
- أدخل راتب 10000 ومكافأة 1000 ودخل إضافي 500.
- earned income يجب أن يساوي 11500.
- أضف إيداعًا 2000 وسحبًا 300.
- صافي الحساب الجاري = +1700.
- لا تدخل الإيداع والسحب في primaryIncome ثم تضفهما مرة أخرى.

## 15.4 اختبار الشهادة
- أصل 100000، معدل 18% سنويًا.
- annualProfit = 18000.
- monthlyEquivalent = 1500.
- quarterly periodicProfit = 4500.
- تأكد من حصرها بين issueMonth وmaturityMonth.
- اختبر مدة بالأشهر ومدة بالسنوات.
- وثّق أن المنطق الحالي يوزع monthlyEquivalent شهريًا، حتى عند دورية ربع سنوية/سنوية.

## 15.5 اختبار الحساب الجاري
- إيداع 5000 وسحب 1200.
- الرصيد = 3800.
- عدّل الإيداع إلى 4000؛ الرصيد يجب أن يصبح 2800 لا 7800.
- احذف السحب؛ الرصيد يصبح 4000.

## 15.6 اختبار التقارير
- يوم/أسبوع/شهر وفترة تاريخية.
- تحقق من نفس rows في المعاينة وCSV وWord والطباعة.
- تحقق من الجمعية: الأقساط سالبة وقبض الجمعية موجب.
- تحقق من عدم إضافة مصروف السيارة مرتين.

## 15.7 اختبار UX
- زر الرجوع يعمل.
- زر الرئيسية العام يظل ظاهرًا وفق متطلبات Smart Time.
- كل زر تعديل يحمّل البيانات الصحيحة.
- كل زر حذف يزيل السجل الصحيح.
- لا يوجد زر صامت.
- لا تضيع البيانات بعد تدوير الشاشة أو إغلاق التطبيق.
- RTL وDark/Light وقياسات الهاتف والتابلت.
- حالات loading/empty/error واضحة.

# 16. قائمة نواقص يجب استكمالها من المصدر قبل ادعاء التوثيق الحرفي 100%

هذه نقاط محددة لا يجوز تخمينها:
1. قراءة `src/components/expenses/AddExpenseModal.tsx` كاملًا لاستخراج كل نص label وترتيب كل زر بدقة في JSX، رغم توثيق حقوله الرئيسية هنا.
2. قراءة `HouseExpensesSection.tsx` كاملًا لاستخراج كل مودالات/أزرار/تصنيفات المنزل، بما فيها أقسام الجمعيات والتفاصيل المتخصصة إن كانت داخله.
3. فحص `PersonalExpensesSection.tsx` والـcomponent الذي يستدعيه لمعرفة موضع نموذج الإدخال الحقيقي.
4. قراءة `WorkExpensesSection.tsx` كاملًا واستخراج خيارات نوع المصروف/المواد/العمالة.
5. قراءة `VehicleExpensesSection.tsx` كاملًا لاستخراج كل أزرار الوقود والصيانة والزيوت وحقولها.
6. قراءة `EducationExpensesSection.tsx` كاملًا وحقول الطالب/المدرسة/الدروس.
7. قراءة `ExpenseEditModal.tsx` كاملًا وتتبع هل يستخدم فعليًا في المسار الحالي أم legacy.
8. استخراج كامل خيارات select في شهادة البنك (المدة والتواتر) وحقول التاريخ/الحفظ/الإلغاء من الأسطر المتبقية في JSX.
9. استخراج كل حقول CarIncomeSection وتواريخها وأوقاتها وأزرار المبالغ السريعة وأنواع التطبيقات كاملة.
10. مطابقة `BankCertificate`, `MonthlyIncome`, `IncomeSourceItem`, `AssociationRecord` مع التعريفات النهائية في `src/types.ts`.
11. فحص backend routes لكل API وتوثيق DTO والملكية والصلاحيات وأخطاء الخادم.
12. فحص ترتيب `DEFAULT_SECTIONS_ORDER` والنصوص والأوصاف الأصلية للبطاقات كما هي.
13. التحقق من الفروع `activeTab='all'` و`menu` ومسارات الشاشة القديمة في ExpensesView.

**لا تُعتبر هذه الوثيقة وحدها إثباتًا أن كل حرف/زر في كل ملفات الأقسام الفرعية قد تم مسحه؛ هي وثيقة تفصيلية موسعة مبنية على JSX والعقود والخدمات المقروءة، وتحتوي قائمة استكمال واضحة.**

# 17. أمر تنفيذ جاهز لأي AI آخر

أنت تستلم قسم SMART TIME المالي. لا تكتفِ بقراءة هذا الملف؛ افتح المستودع الأصلي واقرأ كل الملفات المشار إليها، ثم أنشئ جدول تتبع لكل عنصر UI. لكل عنصر اكتب:
- اسم الشاشة.
- الموضع النسبي (رأس/أعلى/أسفل/داخل بطاقة/صف).
- النص العربي كما يظهر حرفيًا.
- النص الإنجليزي إن وجد.
- اسم المكوّن/المتغير/الحالة في TypeScript.
- اسم handler.
- شروط التفعيل والتعطيل.
- حقول النموذج ونوعها والـvalidation والـdefault.
- أثر الضغط على الحالة.
- API أو Repository أو Storage المستعمل.
- أثر العملية على الإجماليات والتقارير.
- عنصر Compose المقابل.
- اختبار إثبات التنفيذ.

لا تغير مصدر الحقيقة المالي. لا تكرر السجلات. لا تخمن الحقول الناقصة. لا تستبدل الوظيفة بواجهة شكلية. نفذ ثم اختبر CRUD والحسابات والتقارير والتصدير والمزامنة. احتفظ بالهوية الأصلية وRTL وزر الرئيسية الثابت. لا تكتب «مطابق» قبل اختبار واقعي موثق.
