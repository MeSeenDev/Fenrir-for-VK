package dev.ragnarok.fenrir;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.preference.PreferenceManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.umerov.rlottie.RLottieImageView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import dev.ragnarok.fenrir.api.model.VKApiUser;
import dev.ragnarok.fenrir.domain.IOwnersRepository;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.domain.Repository;
import dev.ragnarok.fenrir.link.LinkHelper;
import dev.ragnarok.fenrir.model.Sex;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.settings.ISettings;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;

public class CheckDonate {
    public static final Integer[] donatedUsers = {572488303, 365089125,
            164736208,
            87731802,
            365089125,
            462079281,
            152457613,
            108845803,
            51694038,
            15797882,
            52535002,
            337698605,
            381208303,
            527552062,
            177952599,
            264548156,
            244271565,
            169564648,
            488853841,
            168614066,
            283697822,
            473747879,
            316182757,
            416808477,
            249896431,
            556166039,
            367704347,
            251861519,
            42404153,
            121856926,
            144426826,
            109397581,
            601433391,
            82830138,
            272876376,
            433604826,
            475435029,
            81935063,
            177176279,
            152063786,
            126622537,
            61283695,
            602548262,
            308737013,
            447740891,
            449032441,
            374369622,
            627698802,
            97355129,
            347323219,
            567191201,
            618885804,
            483307855,
            13928864,
            138384592,
            373229428,
            74367030,
            310361416,
            568906401,
            280582393,
            570333557,
            36170967,
            570302595,
            379632196,
            529793550,
            612630641,
            308616581,
            26247143,
            53732190,
            534411859,
            509181140,
            181083754,
            512257899,
            248656668,
            633896460,
            402168856,
            418160488,
            318697300,
            27141125,
            234624056,
            756568,
            337589244,
            335811539,
            514735174,
            137912609,
            544752108,
            107604025,
            175576066,
            177192814,
            430552,
            171784546,
            206220691,
            233160174,
            581662705,
            236637770,
            102082127,
            556649342,
            371502136,
            481394236,
            377667803,
            580434998,
            634164155,
            231369103,
            84980911
    };

    public static boolean isFullVersion(Context context) {
        if (!Constants.IS_DONATE && !Utils.isValueAssigned(Settings.get().accounts().getCurrent(), donatedUsers) && !Utils.isValueAssigned(Settings.get().accounts().getCurrent(), Utils.donate_users)) {
            MaterialAlertDialogBuilder dlgAlert = new MaterialAlertDialogBuilder(context);

            View view = LayoutInflater.from(context).inflate(R.layout.donate_alert, null);
            view.findViewById(R.id.item_donate).setOnClickListener(v -> LinkHelper.openLinkInBrowser(context, "https://play.google.com/store/apps/details?id=dev.ragnarok.fenrir_full"));
            RLottieImageView anim = view.findViewById(R.id.lottie_animation);
            anim.setAutoRepeat(true);
            anim.setAnimation(R.raw.google_store, Utils.dp(200), Utils.dp(200));
            anim.playAnimation();

            dlgAlert.setTitle(R.string.info);
            dlgAlert.setIcon(R.drawable.client_round);
            dlgAlert.setCancelable(true);
            dlgAlert.setView(view);
            dlgAlert.show();
            return false;
        }
        return true;
    }

    public static void isDonated(Activity context, int account_id) {
        Utils.donate_users.clear();
        Utils.donate_users.addAll(Settings.get().other().getDonates());

        //noinspection ResultOfMethodCallIgnored
        InteractorFactory.createDebugToolInteractor().call_debugger()
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(t -> {
                    if (!Utils.isEmpty(t.donates)) {
                        Utils.donate_users.clear();
                        Utils.donate_users.addAll(t.donates);
                        Settings.get().other().registerDonatesId(Utils.donate_users);
                    }
                    MaterialAlertDialogBuilder dlgAlert = new MaterialAlertDialogBuilder(context);
                    boolean isDon = Utils.isValueAssigned(account_id, donatedUsers) || Utils.isValueAssigned(account_id, Utils.donate_users);
                    View view = LayoutInflater.from(context).inflate(R.layout.is_donate_alert, null);
                    ((TextView) view.findViewById(R.id.item_status)).setText(isDon ? R.string.button_yes : R.string.button_no);
                    RLottieImageView anim = view.findViewById(R.id.lottie_animation);
                    anim.setAutoRepeat(true);
                    anim.setAnimation(isDon ? R.raw.is_donated : R.raw.is_not_donated, Utils.dp(200), Utils.dp(200));
                    anim.playAnimation();

                    dlgAlert.setTitle(R.string.info);
                    dlgAlert.setIcon(R.drawable.client_round);
                    dlgAlert.setCancelable(true);
                    dlgAlert.setView(view);
                    dlgAlert.show();
                }, e -> Utils.showErrorInAdapter(context, e));
    }

    public static void UpdateDonateList(Activity context, int mAccountId) {
        Utils.donate_users.clear();
        Utils.donate_users.addAll(Settings.get().other().getDonates());

        //noinspection ResultOfMethodCallIgnored
        InteractorFactory.createDebugToolInteractor().call_debugger()
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(t -> {
                    if (!Utils.isEmpty(t.donates)) {
                        Utils.donate_users.clear();
                        Utils.donate_users.addAll(t.donates);
                        Settings.get().other().registerDonatesId(Utils.donate_users);
                        query(context, mAccountId);
                    }
                }, e -> Utils.showErrorInAdapter(context, e));
    }

    @SuppressLint("SetTextI18n")
    private static void query(Activity context, int mAccountId) {
        if (mAccountId == ISettings.IAccountsSettings.INVALID_ID || Constants.IS_DONATE || Utils.isValueAssigned(mAccountId, donatedUsers) || Utils.isValueAssigned(mAccountId, Utils.donate_users) || !HelperSimple.INSTANCE.needHelp("need_love_help" + mAccountId, 3)) {
            return;
        }

        //noinspection ResultOfMethodCallIgnored
        Repository.INSTANCE.getOwners().getFullUserInfo(mAccountId, mAccountId, IOwnersRepository.MODE_NET)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(pair -> {
                    if (pair.getFirst().getSex() == Sex.WOMAN && pair.getSecond().getCountry() != null && pair.getSecond().getCity() != null && pair.getSecond().getCountry().getId() == 1
                            && (pair.getSecond().getCity().getId() == 1 || pair.getSecond().getCity().getId() == 36 || pair.getSecond().getCity().getId() == 270 || pair.getSecond().getCity().getId() == 1054308)) {
                        SimpleDateFormat SHORT_DATE = new SimpleDateFormat("d.M.yyyy", Locale.getDefault());
                        if (!Utils.isEmpty(pair.getSecond().getBdate())) {
                            try {
                                SHORT_DATE.parse(pair.getSecond().getBdate());
                                int y = SHORT_DATE.getCalendar().get(Calendar.YEAR);
                                if (y < 1996 || y > 2002) {
                                    return;
                                }
                            } catch (ParseException ignore) {
                            }
                        }
                        if (pair.getSecond().getRelation() != VKApiUser.Relation.SINGLE && pair.getSecond().getRelation() != VKApiUser.Relation.SEARCHING && pair.getSecond().getRelation() != VKApiUser.Relation.COMPLICATED && pair.getSecond().getRelation() != 0 || pair.getSecond().getAlcohol() >= 4) {
                            return;
                        }
                        MaterialAlertDialogBuilder dlgAlert = new MaterialAlertDialogBuilder(context);
                        View view = LayoutInflater.from(context).inflate(R.layout.a_is_love_alert, null);
                        ((TextView) view.findViewById(R.id.item_status)).setText("Разработчик клиента ищет себе девушку для серьёзных отношений. Желательно - Очень худенькая (Эктоморф), Фенотип - восточный балтид. Рост - высокий либо средний. Обо мне: 22 года, служил в армии, учусь в МГТУ им. Н.Э. Баумана, программист, люблю языческую мифологию");
                        RLottieImageView anim = view.findViewById(R.id.lottie_animation);
                        anim.setAutoRepeat(true);
                        anim.setAnimation(R.raw.heart, Utils.dp(200), Utils.dp(200));
                        anim.playAnimation();

                        dlgAlert.setTitle(R.string.info);
                        dlgAlert.setIcon(R.drawable.client_round);
                        dlgAlert.setCancelable(false);
                        dlgAlert.setNegativeButton(R.string.button_cancel, null);
                        dlgAlert.setPositiveButton("Открыть профиль", (dialog, which) -> {
                                    PlaceFactory.getOwnerWallPlace(mAccountId, 572488303, null).tryOpenWith(context);
                                    dialog.dismiss();
                                }
                        );
                        dlgAlert.setView(view);
                        dlgAlert.show();
                    }
                }, e -> PreferenceManager.getDefaultSharedPreferences(context).edit().putInt("need_love_help" + mAccountId, 0).apply());
    }
}
