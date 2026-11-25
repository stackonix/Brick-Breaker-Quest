# 15+ Game: Concept, Naam aur Features Doc

Nov 25, 2026 · @Trushit

Ye doc 15 saal aur usse upar ke players ke liye ek offline logic-duel game ka poora plan hai: concept, naam, Play Store SEO, features, UI, kamai aur safety rules. Maine genre khud chuna hai (daily logic puzzle + 60 second ke duels), kyunki aapne sirf "kuch" kaha tha; aap badalna chaho to bata dena.

## 1. Concept: logic duels jo 60 second me hook karte hain

**Game ka idea:** Har din ek naya logic puzzle (sab players ke liye same), aur 60 second ke "duels" jisme aap AI rivals ya doston ke khilaf same puzzle par score compare karte ho. Jeeto to rank badhta hai, aur apna "Brain Profile" (5 skills ka scorecard) dikhta hai jo aap doston ko share kar sakte ho.

**Ye genre kyu chuna**

- Offline chalta hai aur server nahi chahiye (Google AI Studio ke Android apps abhi sirf client-side hain), isliye prototype jaldi banta hai aur kharcha kam.
- India me download ke top par casual aur board-style games hain, jaise Ludo King aur Carrom Pool ([Outlook Respawn](https://respawn.outlookindia.com/gaming/gaming-news/top-10-most-downloaded-mobile-games-in-india-in-2025) ke 2025 install ranking ke hisaab se), kyunki wo seekhne me aasaan aur kam hardware par chalte hain. Aap ka game bhi seekhne me 10 second ka hoga.
- Daily puzzle + streak + share wala format 15+ players me aadat banata hai bina paise ya jua ke.
- Competitive hona 15+ ke liye zaroori hai: dost se aage nikalna sabse bada reason hai dobara khelne ka.

**Game ke 5 pillars**

1. **10 second me samajh:** koi lamba tutorial nahi, pehla puzzle khud sikhata hai.
2. **60 second ka session:** ek duel 60 se 90 second ka, bus ya line me khelne layak.
3. **Fair khel:** sirf skill, koi paisa lagakar jeetna nahi.
4. **Dikhne wali progress:** rank, streak aur Brain Profile har din badhte dikhein.
5. **Share karne layak:** result ka card dosto ko bhejna 1 tap me.

**Kya nahi banana**

- Real money, cash prize ya betting: India ka Online Gaming Act 2025 online money games ko ban karta hai, aur ye game us category me aana hi nahi chahiye (section 9).
- Loot box, jua jaisa spin wheel, ya paise se jeetne wala pay-to-win.
- Anjaan logon se chat ya open voice/text (Play ke rules aur safety dono ki wajah se).

## 2. Audience: 15 se 17 aur 18+ ko alag soch ke saath design karo

15 saal ka student aur 28 saal ka job karne wala dono khelenge, par unki zarooratein alag hain. Ek hi game dono ko pasand aaye iske liye ye farq yaad rakhna hai.

| Baat | 15 se 17 saal | 18 saal aur upar |
| --- | --- | --- |
| Khelne ka samay | School ke baad, raat, chhuttiyan | Commute, lunch break, raat |
| Sabse badi wajah | Dosto se aage nikalna, status | Dimaag tez rakhna, time pass, streak |
| Kya pasand | Chhote duels, rank, share cards | Daily puzzle, hard mode, statistics |
| Kya irritate karta hai | Lamba tutorial, "baby" jaisa look | Bahut ads, zabardasti notifications |
| Safety ke rules | Zyada sakht (section 9: DPDP me ye "child" hain) | Normal adult rules |
| Kamai ka tareeka | Sirf optional aur halka | Optional "Pro" aur rewarded ads |

**3 personas (inke liye features socho)**

- **Aarav, 16, Class 11:** dosto se roz score compare karta hai, share card Instagram story par lagata hai. Usko chahiye: duel, rank, cool dark look.
- **Neha, 21, college:** commute me 10 minute khelti hai, streak toot na jaye iska dar. Usko chahiye: daily puzzle, offline, streak freeze.
- **Rohit, 29, job:** lunch me daily puzzle karta hai, hard mode aur statistics pasand hain. Usko chahiye: no-nonsense UI, Pro mode, koi spam nahi.

**Kya common hai**

- Sab ko "seekhna aasaan, master karna mushkil" wala game chahiye.
- Sab ko chhota session chahiye, jise beech me chhodna aasaan ho.
- Kisi ko bhi "smart nahi ho" jaisa feel nahi karwana hai, galat jawab par bhi halka mazaak, taana nahi.

## 3. Naam aur Play Store SEO

**Pehle sachchi baat:** koi bhi naam Play Store par "pakka number 1" nahi dila sakta. Ranking naam ke saath ratings, crash rate, installs aur backlinks par bhi tiki hoti hai. Naam ka kaam ye hai ki (1) search me aapke keywords pakde, (2) yaad rehne layak ho, aur (3) kisi ke naam se na takraye. "Puzzle game" jaise bade keyword par pehle din #1 nahi aaoge, par "daily logic duel" jaise chhote (long-tail) keywords par top 3 mumkin hai.

**Play Store SEO ke verified facts** ([AppTweak guide](https://www.apptweak.com/en/aso-resources/google-play-store-aso-checklist))

| Jagah | Limit | Kya karna hai |
| --- | --- | --- |
| App title | 30 characters | Unique brand + sabse zaroori keyword |
| Short description | 80 characters | Keyword + download ka reason (conversion) |
| Long description | Limit nahi bataya | Keywords ko 2.5 se 3% density par, bina stuff kiye, bullet points me |
| Developer name | Index hota hai | Brand ka naam, par "best" ya "#1" jaise shabd nahi |
| Ratings aur Android vitals | N/A | Crash aur ANR kam, rating upar: ranking aur featuring dono me madad |
| Category aur tags | N/A | Sabse relevant chuno (Puzzle) |

**Jo naam pehle se liye hue dikhe (isliye avoid karo)**

Maine search me ye naam mil gaye: [Brain Duel](https://appgoblin.info/apps/fm.mystage.brain_duel), [BrainDuel: 7 Mini Brain Games](https://appgoblin.info/apps/com.coderstechno.brainduel), [LOGIC Duel: Math Games](https://appgoblin.info/apps/com.logikduell.app), [Mind Duel (App Store)](https://apps.apple.com/mx/app/mind-duel/id6752247995) aur [IQ Competition - Brain Duel](https://appgoblin.info/apps/fr.dominosoft.qicompetition). "Brain", "Logic" ya "Mind" + "Duel" jaise seedhe naam crowded hain.

**Naam ke candidates**

| # | Title (Play Store par dikhne wala) | Characters | Kyu | Check status |
| --- | --- | --- | --- | --- |
| 1 (meri salah) | Mindfeud: Daily Logic Duel | 26 | Yaad rehne wala unique brand, aur "Daily Logic Duel" teen keywords ek saath | Search me koi match nahi mila, par ye pakka nahi |
| 2 | Dimaag Duel: Brain Puzzles | 26 | Hinglish, India me apna lagta hai, "dimaag" Hindi search ko pakdega | Search me koi seedha match nahi mila, par spelling alag-alag hoti hai (dimaag, dimag), isliye search kam ho sakti hai |
| 3 | Witzy: Logic Puzzle Duels | 25 | Chhota, tez, global | Search me koi match nahi mila, par ye pakka nahi |

**Final karne se pehle ye 5 check zaroor karo** (mere search se pakka nahi hota)

- [ ] Google Play ke search me exact naam daalke dekho ki koi app ya developer wahi naam to nahi use kar raha.
- [ ] India ke trademark registry me naam search karo, aur ek lawyer se pooch lo.
- [ ] Naam ka .com ya .in domain aur Instagram/YouTube handle khaali hai ya nahi.
- [ ] Naam ko 5 doston ko bolkar likhwao: spelling galat likhte hain to naam kamzor hai.
- [ ] Package name (com.brand.game) final karo, kyunki baad me badal nahi sakta.

**Title, short aur long description ka formula**

- **Title (30 char):** `Brand: Main Keyword Phrase`, jaise "Mindfeud: Daily Logic Duel".
- **Short description (80 char):** pehle shabd me sabse zaroori keyword, phir ek faayda. Example: "Daily logic puzzles + 60-sec duels vs bots & friends. Offline. No ads wars." (75 char).
- **Long description:** pehli 2 line me game kya hai aur kyu mazedaar, phir 5 se 7 bullet me features, aur apne keywords (daily logic puzzle, brain duel, offline puzzle) 2.5 se 3% density tak. Hindi ya Hinglish version alag se bhi daalo taaki Hindi search pakde.

**Keywords jo test karne hain (ye mere andaze hain, asli search volume nahi)**

| Keyword | Kyu | Mushkil |
| --- | --- | --- |
| daily logic puzzle | Aapka core hook, kam crowded | Beech ki |
| brain duel game | Seedha intent, par naam wale apps pehle se hain | Zyada |
| offline puzzle game | India me data bachane ka reason | Beech ki |
| iq test game | Bada traffic, par bahut crowded | Bahut zyada |
| dimaag tez karne wala game | Hindi long-tail | Kam |

Asli search volume Play Console ke built-in Search suggestions aur ASO tools (AppTweak, MobileAction jaise) se check karo, aur 2 se 3 hafte me jo keywords chal rahe hain unhe title aur short description me upar lao.

## 4. Features (user-friendly, aur samajhne me aasaan)

Har feature ke saath likha hai ki wo kyu hai aur kis version me aayega. Pehle version (v1) me sirf "Zaroori" wale hi banao.

| Feature | Kya karta hai | Kyu zaroori | Version |
| --- | --- | --- | --- |
| 10 second ka onboarding | Pehla puzzle khud sikhata hai, koi text ka dher nahi | Pehle 30 second me hi zyada log chhod dete hain | v1 (Zaroori) |
| Daily Puzzle | Roz ek naya puzzle, sab ke liye same | Roz wapas aane ki wajah | v1 (Zaroori) |
| 60 sec Duel vs Rival | AI rival ke saath same puzzle par race | Competitive maza, internet nahi chahiye | v1 (Zaroori) |
| Friend Challenge (share link) | Apna score ek link ya image me bhejo, dost wahi puzzle khele | Bina server ke dosto ke saath khelna | v1 (Zaroori) |
| Rank system | Rookie se Legend tak 5 rank | Dikhne wali progress | v1 (Zaroori) |
| Brain Profile | 5 skills ka scorecard (logic, speed, memory, pattern, word) | Share karne layak, self-improvement | v1 (Zaroori) |
| Hint (limited) | Din me 3 free hints | Atakne par chhodne se bachata hai | v1 (Zaroori) |
| Streak aur Streak Freeze | Roz khelne par streak, ek din chhute to freeze se bachao | Habit banata hai bina dabav ke | v1 (Zaroori) |
| Dark aur Light mode | System ke hisaab se | 15+ ko raat me khelna pasand hai | v1 (Zaroori) |
| Offline mode | Poora game bina internet ke | India me data aur network ka masla | v1 (Zaroori) |
| Hindi aur English | Poora game dono bhasha me | Zyada log samajh payenge | v1 (Zaroori) |
| Settings | Sound, vibration, notification, language, theme | Control ka ehsaas | v1 (Zaroori) |
| Weekly League | Hafte bhar ka score, ghost rivals ke saath | Lamba goal | v1.1 |
| Brain Stats | Pichle 30 din ka graph | Adults ko pasand | v1.1 |
| Hard mode aur Pro | Mushkil puzzle, ads nahi | Kamai ka raasta | v1.1 |
| Achievements | 30 badges | Collector log ke liye | v1.1 |
| Cloud save aur sign-in | Phone badalne par progress | Tabhi jab zarurat ho, data ka bojh badhta hai | v2 |

**User-friendly rules (har screen par lagu)**

- Ek screen par ek hi mukhya button, aur wo sabse bada aur sabse chamkila.
- Koi bhi button 48dp se chhota nahi (Android ki guideline), aur bade angoothe se dabane layak.
- Back dabane par galti se bahar na nikle, "Chhodna hai?" ka ek pop-up aaye jisme "Khelte raho" pehle ho.
- Har error ka message insaan ki bhasha me, jaise "Internet nahi hai, par aap offline khel sakte ho".
- Koi bhi popup ya ad game ke beech me na aaye, sirf duel khatam hone ke baad.
- Bina login, bina phone number, bina email ke khelna shuru ho.
- Notification roz ek se zyada nahi, aur Settings me ek tap me band ho.

**Onboarding (pehli baar ke 30 second)**

1. Splash screen (1.5 second) me logo.
2. Seedha ek puzzle: ek chhota haath ka ishara dikhata hai kya karna hai, koi lamba text nahi.
3. Jeetne par confetti aur "Ye tha aapka pehla duel!" ka message.
4. Fir ek sawaal: "Kaise khelna pasand hai?" (Daily puzzle / Duel / Dono) aur uske hisaab se home screen.
5. Notification ki permission baad me maango, jab user 3 din khel chuka ho, shuru me nahi.

## 5. Gameplay aur modes

**Ek duel kaise chalta hai (60 se 90 second)**

1. Rival chuno (ya random) aur 3 second ka countdown.
2. 5 chhote puzzles ek ke baad ek, har ek 10 se 20 second ka.
3. Sahi jawab par points, aur jitni jaldi, utne zyada (par sahi hona speed se zyada points deta hai).
4. Duel ke end me score compare: win, draw ya loss.
5. Result screen par XP, rank progress, aur "Rematch" ka bada button.

**Puzzle types (6 se shuru, baad me badhao)**

| Puzzle | Kya karna hai | Skill |
| --- | --- | --- |
| Number Chain | Agla number batao (pattern) | Logic |
| Code Breaker | 4 rang ka secret code, hints se guess (Mastermind jaisa) | Deduction |
| Odd Shape | 6 shapes me alag wala dhundo | Pattern |
| Quick Calc | 3 operations me target number tak pahucho | Speed |
| Word Twist | 6 akshar se sabse lamba shabd | Word |
| Memory Grid | Pattern 3 second dekho, fir wahi dohrao | Memory |

**Modes**

| Mode | Kaisa hai | Kab khulta hai |
| --- | --- | --- |
| Daily Puzzle | Roz ek bada puzzle, sab ke liye same, ek hi koshish (par practice alag) | Pehle din se |
| Duel vs Rival | 60 second ka race AI ke saath | Pehle din se |
| Friend Challenge | Dost ko link bhejo, wo wahi 5 puzzles khele, score compare | Pehle din se |
| Practice | Bina rank ke, koi time nahi | Pehle din se |
| Weekly League | Hafte bhar ka score, ghost rivals | v1.1 |

**Rivals (AI) ki personality**

- 4 rivals har ek ki apni speed aur galti ki dar: "Quick Qasim" (tez par galti karta hai), "Calm Kavya" (dheere par sahi), "Pattern Pranav" (pattern me strong, word me kamzor), "Boss Bhavna" (sab me balanced, sabse mushkil).
- Rival ki difficulty player ke rank se match hoti hai, aur 3 baar haarne par thoda aasaan hota hai, taaki frustration na ho.
- Rivals ke naam original rakho, kisi mashhoor insaan ya character se mel nahi.

**Ranks aur progress**

| Rank | XP | Unlock |
| --- | --- | --- |
| Rookie | 0 | Basic puzzles |
| Thinker | 500 | Code Breaker, Rival 2 |
| Strategist | 1500 | Weekly League, themes |
| Mastermind | 4000 | Hard mode, Rival 4 |
| Legend | 10000 | Special badge aur profile frame |

XP har duel aur daily puzzle se milta hai. XP numbers abhi ke andaze hain, playtest me balance karna padega.

**Fairness ke niyam**

- Daily puzzle sabke liye ek hi hoga, taaki Friend Challenge fair rahe.
- Daily puzzle ka din app ke andar yaad rakho: agar phone ki date pichhe kar di jaye to naya Daily Puzzle na khule aur streak na badhe. Offline me ye poori tarah rok nahi sakte, isliye ise sirf halka cheat-rokne wala niyam maano.
- Hint ya streak freeze kharidne se jeet nahi milti: wo sirf atakne ya streak bachane me madad karte hain.

## 6. UI/UX aur colors

15+ players ko bachkana look pasand nahi aata. Look: "clean aur thoda gaming" jaisa: dark background, neon accents, saaf bade numbers, aur chhoti smooth animations. Dark mode default, Light mode option.

**Screens**

| Screen | Isme kya | Khaas rule |
| --- | --- | --- |
| Splash | Logo, 1.5 second | Skip ya wait nahi |
| Onboarding puzzle | Pehla puzzle, haath ka ishara | Koi form nahi |
| Home | Daily Puzzle ka bada card, "Duel" ka bada button, streak aur rank upar | Ek hi mukhya button |
| Duel lobby | Rival chuno, rank match | Ek tap me start |
| Puzzle screen | Beech me puzzle, upar timer aur score, neeche hint | Puzzle ka area 65% se zyada |
| Result | Win ya loss, score, XP, Rematch aur Share | Share card ek tap me |
| Brain Profile | 5 skills ka scorecard, rank | Screenshot-friendly |
| Friend Challenge | Link ya image banao, bhejo | Android share sheet use karo |
| Settings | Sound, vibration, notification, language, theme, privacy policy | Sab ek hi list me |

**Dark theme palette (default)**

Neeche ke contrast numbers maine check kiye hain (WCAG ke hisaab se normal text ke liye 4.5 ya zyada, bade text aur icons ke liye 3 ya zyada).

| Kaam | Naam | Hex | Contrast |
| --- | --- | --- | --- |
| Background | Midnight | #0F1226 | Safed text (#F4F5FF) par 17.1 |
| Cards | Deep Navy | #1B1F3B | Safed text par 14.8 |
| Mukhya text | Ghost White | #F4F5FF | Base |
| Dusra text | Soft Lavender | #A9AEDB | Cards par 7.5 |
| Mukhya button, links | Electric Cyan | #22D3EE | Background par 10.2 |
| Magic, rank, profile | Violet | #A78BFA | Background par 6.8 |
| Sahi jawab, jeet | Mint | #34D399 | Background par 9.6 |
| Galat jawab, haar (halka) | Coral | #FB7185 | Background par 6.9 |
| Streak, gems, stars | Gold | #FBBF24 | Background par 11.1 |

**Light theme ke niyam**

- Background #F6F7FF, text #14172F (16.5 contrast), dusra text #555B8A (6.1).
- Mukhya button: Violet Deep #5B3FD0 safed text ke saath (6.8). Light background par green, coral aur cyan sirf bade icons ya shapes me use karo, chhote text me nahi (unka contrast 4.1 se 4.4 hai, jo chhote text ke liye kam hai).

**Rang aur style ke niyam**

- Ek screen par 3 se zyada accent rang nahi.
- Galat jawab par lal rang ki jagah halka Coral aur chhota shake, taaki dard na lage.
- Rang ke saath shape ya icon bhi (tick aur cross), taaki color-blind players ko dikkat na ho.
- Font: ek gol-sans (Poppins ya Inter jaise, dono open license), numbers ke liye tabular figures taaki timer hilta na dikhe.
- Text size 16sp se kam nahi, timer aur score 28sp se bade.
- Animation: tap par 100 se 150 millisecond, jeet par confetti 700 millisecond. Bahut tez flash (1 second me 3 baar se zyada) mat rakho.
- Haptic vibration halka, aur Settings me band karne ka option.

**Accessibility**

- TalkBack ke liye har button ka label, aur "Reduce motion" setting par animations kam.
- Font size system ke hisaab se badhe, aur layout na toote.
- Hindi me Devanagari text ki jagah alag se check karo, kyunki wo English se zyada jagah leta hai.

## 7. Retention: log roz kyu wapas aayenge

Retention ka matlab hai ki log install ke baad bhi khelte rahein, aur ye hi ranking, ratings aur kamai teeno ki jaan hai. Neeche ke tareeke dabav ya lat par nahi, maze aur aadat par bane hain, kyunki 16 se 17 saal ke apps me Play ke rules "addictive elements" ko mana karte hain.

| Kab | Kya hota hai | Maqsad |
| --- | --- | --- |
| Pehle 30 second | Seedha puzzle, jaldi jeet | Pehle din ka dropout kam karna |
| Pehla session | 3 duel khelne par pehla badge aur ek Brain Profile card | "Mujhe kuch mila" ka ehsaas |
| Din 1 | Daily puzzle ki shaam ko ek soft reminder (agar user ne allow kiya) | Dobara aana |
| Din 3 | Notification permission maango, aur ek Streak Freeze gift | Aadat banana |
| Din 7 | Pehla rank up aur ek naya puzzle type unlock | Naya kuch milna |
| Din 14 aur 30 | Brain Profile ka comparison ("pichle hafte se 12% tez") | Seekhne ka sabut |

**Roz ke hooks**

- Daily Puzzle: ek bada puzzle roz, aur uska result share card ban jata hai.
- Streak: lagatar din ginte hain, aur 7, 30, 100 din par badge.
- Streak Freeze: mahine me 2 free, jisse ek din chhootne par bhi streak na tute.
- Friend Challenge: dost ko "main tumse aage hu" ka link, jo sabse sasta aur asardaar growth hai.

**Social, bina chat ke**

- Sirf share card, link aur score compare. Koi open chat, DM ya anjaan logon se baat nahi.
- Share card par naam ki jagah "Player" ya jo nickname user khud likhe, aur nickname me gaaliyan ya numbers filter ho.
- Share link me user ka koi personal data nahi.

**Notifications ke niyam**

- Din me 1 se zyada nahi, aur raat 9 baje ke baad ya subah 8 baje se pehle nahi.
- Sirf kaam ki baat: "Aaj ka puzzle taiyaar hai", "Streak 6 din ki, aaj khelo?".
- Dar ya sharm wale message nahi, jaise "Tumhari streak toot jayegi!" ya "Tum haar gaye".
- Settings me ek tap me band.

**Kya nahi karna (healthy rakhne ke liye)**

- Infinite session ya "bas ek aur" ka jaal: 20 duel ke baad halka sa "Aaj ke liye kaafi?" message.
- Timer ke dabav me paise kharchwana.
- Doosron se compare karke chhota feel karwana: sirf apna pichla score se compare dikhao.

**Metrics jo roz dekhne hain (targets mere andaze hain)**

| Measure | Target | Kyu |
| --- | --- | --- |
| Day-1 wapasi | 30% se upar | Pehla anubhav kaisa raha |
| Day-7 wapasi | 10% se upar | Aadat ban rahi hai ya nahi |
| Day-30 wapasi | 4% se upar | Lambi aayu |
| Ek din ke sessions | 3 se 5 | Chhote sessions wala game |
| Crash-free sessions | 99.5% se upar | Ranking aur reviews |
| Play Store rating | 4.3 se upar | Ranking aur conversion |

## 8. Kamai: seedhi, saaf aur 15 se 17 ke liye safe

Sabse safe raasta: pehle version me koi ads nahi, aur kamai sirf ek optional "Pro" se. Isse privacy aur Play ke rules ka bojh kam rehta hai, aur ads ki wajah se ranking aur ratings girne ka khatra bhi.

| Raasta | Kaise | Fayda | Khatra | Meri salah |
| --- | --- | --- | --- | --- |
| Free + ek baar "Pro" purchase | Hard mode, stats, extra themes, ads-free | Saaf, bharose wala | Pehle kam log kharidenge | v1.1 me shuru, sabse accha balance |
| Rewarded ads (optional) | Hint ya Streak Freeze ke liye user khud ad dekhe | User ki marzi par | Sirf non-personalized ads aur sahi certified SDK hi | v1.1, pehle se nahi |
| Banner ya interstitial ads | Screen par ya beech me | Zyada impression | Game ka maza bigadta hai, reviews girte hain | Mat lagao |
| Subscription | Mahine ka paisa | Lagatar kamai | Yuvaon ko kharide ke baad pachhtawa, aur Play ke "high spend" rules | Abhi mat lagao |

**Pro ki keemat ka idea (test karna hai, pakka nahi)**

- Ek baar ka chhota paisa, jaise Rs. 49 se Rs. 149 ke beech, aur 2 se 3 keemat A/B test karke dekhna.
- Free users ko bhi roz ka Daily Puzzle aur Duel poora milna chahiye, taaki Pro "zabardasti" na lage.

**Kamai ke niyam**

- Pro purchase se pehle ek saaf confirmation: kya milega aur kitne paise. Koi chhupa hua auto-renewal nahi.
- Real-money prize, cash reward, ya betting nahi: wo India me online money games me aate hain jo ban hain (section 9).
- Virtual cheezein (gems, coins) ko asli paise se saaf alag dikhao, aur gems ko asli paise me badalne ka option mat do.
- 18 se kam players ke liye bade ya baar-baar wale purchases se bacho. Google ke 16 se 17 saal wale apps me "high spend thresholds" aur addictive elements ko mana kiya gaya hai.
- Agar ads lagao to: sirf non-personalized ads, aur ad SDK Google Play ke Families Self-Certified list me hona chahiye (jab tak Play Console aur lawyer confirm na karein ki aapke case me zaroori nahi).

## 9. Safety aur policy: 15 se 17 saal ke players ki wajah se ye zaroori hai

Is game ka aadha audience 18 se kam ho sakta hai, aur India ke data law me 18 se kam "child" hai. Isliye safety ko design ka hissa banao, baad me jodna mushkil hota hai. Ye legal salah nahi hai: launch se pehle ek lawyer se privacy policy aur Play Console ke jawab check karwao.

**1. Google Play: age groups**

- Play Console me target age groups ye hain: 5 saal aur kam, 6-8, 9-12, 13-15, 16-17, aur 18 aur upar ([Play Help](https://support.google.com/googleplay/android-developer/answer/9867159?hl=en)).
- 15 saal ke players ke liye aapko "13-15" group chunna padega. Play kehta hai ki 13-15 aur 16-17 groups kuch jagahon par "children" me ginne ja sakte hain, aur us case me Families rules lagu ho sakte hain. Isliye game ko kam se kam data aur sakht rules ke saath design karo.
- Group sirf tabhi chuno jab game sach me us umar ke liye suitable ho. Galat chunna app hatwa sakta hai.
- 13-15 ke liye Play ki guideline: koi violence ya sexual content nahi, risky challenges ko badhava nahi. 16-17 ke liye: koi addictive elements ya high spend thresholds nahi.
- Agar ads dikhao aur target me bachhe (ya anjaan umar ke users) shamil hon, to ads sirf Families Self-Certified SDK se, aur ek neutral age screen chahiye (jahan user se seedha birth date nahi poocha jata aur pehle se bhari date nahi hoti).
- Agar aap sirf 18+ chunte ho to "Restrict Minor Access" on ho sakta hai, jisse 18 se kam users app download hi nahi kar sakte. Par tab aapka 15 se 17 saal wala audience khatam ho jayega, isliye ye alag faisla hai (niche dekho).

**2. India: DPDP Act aur Rules**

- 18 se kam umar ke bachhon ka personal data process karne se pehle parent ki verifiable consent chahiye ([Bass Berry](https://www.bassberry.com/news/indias-data-privacy-rules-what-your-business-needs-to-know/)).
- Rules 13 November 2025 ko final hue, aur zyadatar obligations 18 mahine baad, yani lagbhag May 2027 tak lagu hongi ([Legal500](https://www.legal500.com/firms/237811-saga-legal/c-india/news-and-developments/the-privacy-timer-starts-now-analysing-the-dpdp-rules-and-its-18-month-countdown)).
- Bachhon par tracking ya behavioural monitoring aur targeted advertising par roka-tok hai, kuch alag-alag exemptions ke saath ([Mondaq](https://webiis10.mondaq.com/india/data-protection/1706722/legal-update-digital-personal-data-protection-rules-2025)).
- Isliye sabse aasaan raasta: koi personal data hi mat lo, aur koi tracking ya personalized ads mat lagao.

**3. India: Online Gaming Act 2025**

- Online money games (jisme paisa lagakar jeetne ki umeed ho) par pabandi hai, chahe skill ka khel ho ya chance ka. Social games jisme koi paisa ya stake nahi, wo allowed hain ([Nishith Desai](https://www.nishithdesai.com/Default.aspx?id=15427)). Ye article bill ke paas hone ke samay ka hai, isliye abhi ka notified rule aur registration ka tareeka lawyer se confirm karo.
- Isliye game me cash prize, paid entry ya betting nahi.

**Data ka design (privacy-by-design)**

| Cheez | Faisla |
| --- | --- |
| Login, naam, email, phone | v1 me nahi, sirf optional nickname |
| Birthday | Poochna ho to sirf neutral age screen par, aur save nahi |
| Location, contacts, mic, camera | Permission hi nahi maangni |
| Progress, streak, rank | Phone me local save |
| Analytics | v1 me koi third-party SDK nahi, ya sirf bina advertising ID wala, bachhon ke liye approved |
| Share link | Sirf puzzle ki ID aur score, koi personal data nahi |
| Personalized ads aur tracking | Nahi |

**Aap ke liye ek bada faisla**

| Option | Fayda | Nuksan |
| --- | --- | --- |
| A. 13-15, 16-17 aur 18+ teeno chuno (15 se upar sabko lo) | Poora audience, 15 saal wale bhi | Sakht rules (data kam, ads kam), DPDP ka parent consent baad me aa sakta hai |
| B. Sirf 18+ chuno aur Restrict Minor Access on karo | Compliance aasaan | 15 se 17 saal wale download nahi kar payenge, jo aapka maqsad tha |

Meri salah: Option A, par game ko B ke jitna safe design karo (koi chat, koi data, koi personalized ads nahi). Ye faisla aap ka hai, aur lawyer se confirm karo.

## 10. Tech aur roadmap

**Kis cheez se banaye**

- **Prototype (hafta 1 se 2):** Google AI Studio ka Android Build mode. Ye Kotlin aur Jetpack Compose me native app banata hai aur Play ke internal testing track tak publish kar deta hai ([Google docs](https://ai.google.dev/gemini-api/docs/aistudio-android)). Dhyan rakho: AI Studio ke Android apps abhi sirf client-side hain (yaani koi apna server nahi), aur free plan me roz ki limit hoti hai.
- **Asli game (hafta 3 se aage):** prototype ka ZIP Android Studio me kholo aur Compose me aage badhao. Is game me tap, timer aur chhote animation hain, jo Compose me aasaani se ban jate hain.
- Agar baad me iPhone ya bahut zyada effects chahiye, to Godot ya Unity par jao.

**Architecture (layers alag rakho)**

- **UI:** har screen alag Compose screen, colors aur sizes ek theme file me (section 6 ke hex).
- **Puzzle engine:** har puzzle type ka generator aur checker alag, bina UI ke, taaki unit tests chal sakein.
- **Daily puzzle:** puzzle ek fixed seed se bane (jaise din ka number), taaki sab ko wahi mile, aur Friend Challenge me wahi seed share ho. Seed ek hi list se aaye jo app ke saath aati hai (ya baad me update se).
- **Rival (AI):** bas ek simple model: rival ka har puzzle par jawab dene ka time aur galti ki probability, bina kisi server ke.
- **Storage:** progress, streak aur rank phone me local (DataStore).
- **Share:** Android ki share sheet, ek image card aur ek deep link.

**Roadmap (idea se launch tak, lagbhag 16 hafte, akele part-time ke hisaab se andaza)**

| Phase | Hafta | Kya banta hai | Gate (aage badhne ki shart) |
| --- | --- | --- | --- |
| 0. Idea lock | 1 | 1 page ka brief, naam shortlist aur check | Naam ke 5 check pass |
| 1. Prototype | 1 se 3 | 3 puzzle types + 1 duel | 10 players me 6 "ek aur" bolen |
| 2. Design | 3 se 5 | Style guide, 9 screens ke mockup | 5 players bina madad ke duel khatam karein |
| 3. Alpha | 4 se 10 | 6 puzzle types, modes, ranks, daily puzzle | Poora game start se end tak chale, koi crash nahi |
| 4. Hindi + polish | 9 se 12 | Hindi text, sound, animation, share card | Har screen dono bhasha me theek |
| 5. Testing | 11 se 14 | 6 phones par QA, 20 players ka playtest | Crash-free 99.5% se upar |
| 6. Store + compliance | 13 se 15 | Listing, privacy policy, Data safety, age group | Saare Play Console forms complete |
| 7. Closed test se production | 14 se 18 | 12 se zyada testers, 14 din lagatar | Production access mil jaye |
| 8. Launch aur updates | 18 se aage | Staged rollout, v1.1 | 2 hafte crash aur ANR stable |

**Playtest ka tareeka (15+ ke saath)**

- 15 se 17 saal ke players ke parent ki ijazat lo.
- 10 se 20 logon ko do group me baanto (15 se 17, aur 18 se upar) aur dekho ki kahan atakte hain, kahan has dete hain, aur kab khelna chhod dete hain.
- Har session ke baad 3 sawaal: "Sabse mazedaar kya laga?", "Kahan samajh nahi aaya?", "Kal dobara khelte?"

## 11. Launch aur ranking plan

**#1 ke baare me sachchi umeed**

- Pehle hi din "puzzle game" ya "brain game" jaise bade keyword par #1 aana lagbhag nahi hota, kyunki wahan purane aur bade apps hain.
- Realistic goal: "daily logic puzzle", "logic duel" jaise chhote keywords par top 3 se 10, aur phir naye keywords jodte jaana.
- Ranking me naam ke alawa ratings, crash rate aur Android vitals, category aur tags, aur bahar se aane wale links bhi kaam karte hain ([AppTweak](https://www.apptweak.com/en/aso-resources/google-play-store-aso-checklist)). Isliye game ki quality hi sabse bada SEO hai.

**Play Console ka zaroori rule**

Agar aapka personal developer account 13 November 2023 ke baad bana hai, to production se pehle closed test me kam se kam 12 testers chahiye jo 14 din lagatar opt-in rahe ([Play Help](https://support.google.com/googleplay/android-developer/answer/14151465?hl=en)). Isliye launch se kam se kam 3 hafte pehle testers jodna shuru karo.

**Launch checklist**

- [ ] Naam, package name aur developer name final, aur trademark check ho gaya
- [ ] Target age groups (13-15, 16-17, 18+) aur Data safety, content rating (IARC) sahi bhare
- [ ] Privacy policy ek public page par, aur app ke Settings me link
- [ ] Title (30), short description (80) aur long description keywords ke saath, English aur Hindi
- [ ] App icon, feature graphic aur 6 se 8 screenshots jisme asli gameplay dikhe
- [ ] 30 second ka gameplay video
- [ ] Closed test: 20 se 25 testers, 14 din lagatar
- [ ] Crash-free 99.5% se upar, aur 6 phones par test
- [ ] Staged rollout 10%, fir 25%, 50%, 100%
- [ ] Launch ke baad roz Android vitals aur reviews dekhna

**Store listing me kya dikhao**

- Pehla screenshot: "60 second ka duel" ka ek action shot, saaf bade text ke saath.
- Dusra: Daily Puzzle aur streak. Teesra: Brain Profile ka share card. Chautha: Friend Challenge.
- Screenshots me text 4 se 6 shabd ka, taaki chhote screen par bhi padhe.
- Icon: ek simple, bold shape (jaise do takraate bolt ya do arrow), 2 rang me, jo chhote size par bhi pehchana jaye.

**Launch ke baad ke 30 din**

| Hafta | Kaam |
| --- | --- |
| 1 | Crash aur reviews ka jawab, 10% se 25% rollout |
| 2 | Keyword data dekho, title aur short description tweak karo |
| 3 | 1 chhota update (naya puzzle type ya level), release notes me achhe likho |
| 4 | Pro purchase ya rewarded ads ka test (v1.1), aur ratings maangne ka shaant tareeka: jeet ke baad ek baar |

**Bahar se log laane ke free tareeke**

- College aur coaching groups (Telegram, WhatsApp, Discord), jahan 15 se 25 saal log hain, aur Friend Challenge ke links.
- Instagram Reels aur YouTube Shorts par "mere dost ko 60 second me haraya" wale clips.
- Ek chhoti website ya landing page jo Play Store ka link rakhe, jisse backlink bane.
- Reviews ke liye paise ya inaam mat do, aur fake reviews ya fake installs mat lo: Play policy ke khilaf hai aur app hat sakta hai.

## 12. Risks aur agle kadam

| Risk | Asar | Ilaaj |
| --- | --- | --- |
| Naam kisi se takraye | Rebrand ya takedown | Section 3 ke 5 checks launch se pehle |
| Bade keyword par rank nahi aana | Kam installs | Long-tail keywords aur quality par focus |
| 15 se 17 saal ke data ke rules | Play ya DPDP me problem | Kam se kam data, koi tracking nahi, lawyer check |
| Online Gaming Act ki galat samajh | Legal masla | Koi paisa ya prize nahi, aur registration ka rule lawyer se confirm |
| 12-tester, 14-din ka rule | Launch 2+ hafte late | Phase 5 se hi testers jodna shuru karo |
| AI Studio free limit | Prototype dheere | Kaam chhote hisson me baanto, ya Android Studio par jao |
| Puzzles repeat lagne lage | Retention girti hai | Puzzle generators se naye puzzle, har hafte content |
| AI rivals bahut mushkil ya bahut aasaan | Frustration ya bore | Rank se difficulty match, playtest me tune |
| Phone ki date badalkar cheating | Fairness toot ti hai | Streak ka din app me yaad rakho, aur share link me answer kabhi na dikhe |

**Is hafte ke agle kadam**

- [ ] Naam ke top 3 candidates par section 3 ke 5 checks chalao
- [ ] Google AI Studio kholo, Android chip chuno, aur Phase 1 prototype prompt chalao (3 puzzle types + 1 duel)
- [ ] 10 players ki list banao (5 ki umar 15 se 17, 5 ki 18 se upar) aur 15 se 17 wale ke parent ki ijazat lo
- [ ] Play Developer account ka registration shuru karo, kyunki verification me samay lagta hai
- [ ] Faisla karo: 13-15 + 16-17 + 18+ (Option A) ya sirf 18+ (Option B)

**Aap se jo jaan na hai taaki plan aur tez ho**

- Genre aap ka dimaag me kya tha? Main ne logic duel chuna hai, par aap ko racing, quiz, strategy ya kuch aur chahiye to batao, plan us hisaab se badal jayega.
- Akele banayenge ya team hai?
- Sirf India me launch ya duniya bhar me? (Duniya bhar me US ka COPPA aur EU ke rules bhi dekhne honge.)
- iPhone version bhi chahiye?

**Sources**

- [Play Help: Target audience and app content settings](https://support.google.com/googleplay/android-developer/answer/9867159?hl=en)
- [Play Help: Families Policy requirements](https://support.google.com/googleplay/android-developer/answer/9893335?hl=en)
- [Play Help: App testing requirements for new personal accounts](https://support.google.com/googleplay/android-developer/answer/14151465?hl=en)
- [AppTweak: Google Play ASO checklist](https://www.apptweak.com/en/aso-resources/google-play-store-aso-checklist)
- [Google AI Studio: Build Android apps](https://ai.google.dev/gemini-api/docs/aistudio-android)
- [Outlook Respawn: top downloaded mobile games in India 2025](https://respawn.outlookindia.com/gaming/gaming-news/top-10-most-downloaded-mobile-games-in-india-in-2025)
- [Nishith Desai: Promotion and Regulation of Online Gaming Bill, 2025](https://www.nishithdesai.com/Default.aspx?id=15427)
- [Bass Berry: India's DPDP Rules](https://www.bassberry.com/news/indias-data-privacy-rules-what-your-business-needs-to-know/)
- [Legal500: DPDP Rules 18-month timeline](https://www.legal500.com/firms/237811-saga-legal/c-india/news-and-developments/the-privacy-timer-starts-now-analysing-the-dpdp-rules-and-its-18-month-countdown)
- [Mondaq: DPDP Rules 2025 legal update](https://webiis10.mondaq.com/india/data-protection/1706722/legal-update-digital-personal-data-protection-rules-2025)
