import { useEffect, useState } from 'react';
import {
  Bell,
  CloudDownload,
  Copy,
  Eye,
  EyeOff,
  LogOut,
  Plus,
  ShieldCheck,
  X,
} from 'lucide-react';
import { developerAuthManager } from '../services/developerAuthManager';
import { developerContentManager } from '../services/developerContentManager';

interface DeveloperDashboardDialogProps {
  onDismiss: () => void;
  onShowToast: (msg: string) => void;
}

const TABS = [
  'القراء والتلاوات',
  'الإشعارات',
  'الإعلانات',
  'النسخ الاحتياطي',
];

export function DeveloperDashboardDialog({
  onDismiss,
  onShowToast,
}: DeveloperDashboardDialogProps) {
  const [authState, setAuthState] = useState(() =>
    developerAuthManager.getState(),
  );
  const [contentState, setContentState] = useState(() =>
    developerContentManager.getState(),
  );

  const [inputEmail, setInputEmail] = useState(authState.developerEmail);
  const [inputPassword, setInputPassword] = useState('');
  const [authError, setAuthError] = useState<string | null>(null);
  const [selectedTabIndex, setSelectedTabIndex] = useState(0);

  // Add reciter form state
  const [showAddForm, setShowAddForm] = useState(false);
  const [newName, setNewName] = useState('');
  const [newRiwayah, setNewRiwayah] = useState('حفص عن عاصم');
  const [newServerUrl, setNewServerUrl] = useState(
    'https://server10.mp3quran.net/',
  );

  // Notification form state
  const [notifTitle, setNotifTitle] = useState('تذكير بتلاوة القرآن الكريم');
  const [notifMessage, setNotifMessage] = useState(
    'قال رسول الله ﷺ: «اقرؤوا القرآن فإنه يأتي يوم القيامة شفيعاً لأصحابه».',
  );

  useEffect(() => {
    const unsubAuth = developerAuthManager.subscribe(() => {
      setAuthState(developerAuthManager.getState());
    });
    const unsubContent = developerContentManager.subscribe(() => {
      setContentState(developerContentManager.getState());
    });
    return () => {
      unsubAuth();
      unsubContent();
    };
  }, []);

  const handleAuthSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!authState.isAccountSetup) {
      const ok = await developerAuthManager.setupDeveloperAccount(
        inputEmail,
        inputPassword,
      );
      if (ok) {
        setAuthError(null);
      } else {
        setAuthError('يرجى كتابة بريد صالح وكلمة مرور من 6 خانات على الأقل.');
      }
    } else {
      const ok = await developerAuthManager.login(inputEmail, inputPassword);
      if (ok) {
        setAuthError(null);
      } else {
        setAuthError(
          'بيانات الدخول غير صحيحة. يرجى التأكد من البريد وكلمة المرور.',
        );
      }
    }
  };

  const handleSendNotification = async () => {
    const title = notifTitle.trim() || 'تذكير التلاوة المباركة';
    const body =
      notifMessage.trim() || 'لا تنسَ تلاوة وردك اليومي من القرآن الكريم.';

    if (typeof window !== 'undefined' && 'Notification' in window) {
      if (Notification.permission === 'granted') {
        new Notification(title, {
          body,
          icon: '/images/ic_quran_app.jpg',
        });
        onShowToast('تم إرسال الإشعار بنجاح!');
        return;
      }
      if (Notification.permission !== 'denied') {
        const perm = await Notification.requestPermission();
        if (perm === 'granted') {
          new Notification(title, {
            body,
            icon: '/images/ic_quran_app.jpg',
          });
          onShowToast('تم إرسال الإشعار بنجاح!');
          return;
        }
      }
    }
    onShowToast(`${title}: ${body}`);
  };

  const handleCopyBackup = async () => {
    const json = developerContentManager.exportBackupJson();
    try {
      await navigator.clipboard.writeText(json);
      onShowToast('تم نسخ النسخة الاحتياطية إلى الحافظة بنجاح!');
    } catch {
      onShowToast('تعذر النسخ التلقائي؛ يمكنك نسخ النص الظاهر يدوياً.');
    }
  };

  const backupJson = developerContentManager.exportBackupJson();

  return (
    <div
      className="fixed inset-0 z-50 flex items-end sm:items-center justify-center bg-black/65 backdrop-blur-sm"
      onClick={onDismiss}
    >
      <div
        data-testid="developer_dashboard_sheet"
        className="w-full sm:max-w-xl h-[90vh] flex flex-col rounded-t-3xl sm:rounded-3xl border px-5 pb-6 pt-3 shadow-2xl"
        style={{
          backgroundColor: 'var(--islamic-bg)',
          borderColor: 'var(--islamic-border)',
        }}
        onClick={(e) => e.stopPropagation()}
      >
        <div
          className="w-11 h-1 rounded-full mx-auto mb-3 shrink-0"
          style={{ backgroundColor: 'var(--islamic-border)' }}
        />

        {/* Header */}
        <div className="flex items-center justify-between gap-2 mb-4 shrink-0">
          <div className="flex items-center gap-2.5 min-w-0">
            <div
              className="w-10 h-10 rounded-full border flex items-center justify-center shrink-0"
              style={{
                backgroundColor: 'var(--islamic-emerald-dark)',
                borderColor: 'var(--islamic-gold)',
              }}
            >
              <ShieldCheck size={20} style={{ color: 'var(--islamic-gold)' }} />
            </div>
            <div className="min-w-0">
              <h2
                className="font-amiri font-bold text-xl leading-tight"
                style={{ color: 'var(--islamic-gold-light)' }}
              >
                لوحة تحكم المطوّر
              </h2>
              <p
                className="font-cairo text-[11px] truncate"
                style={{ color: 'var(--islamic-text-secondary)' }}
              >
                {authState.isLoggedIn
                  ? `حساب مصرح: ${authState.developerEmail}`
                  : 'إدارة محتوى وإعدادات التطبيق'}
              </p>
            </div>
          </div>

          <div className="flex items-center gap-1 shrink-0">
            {authState.isLoggedIn && (
              <button
                type="button"
                onClick={() => {
                  developerAuthManager.logout();
                  setAuthError(null);
                  setInputPassword('');
                }}
                aria-label="تسجيل خروج"
                data-testid="dev_logout_button"
                className="w-10 h-10 rounded-full flex items-center justify-center cursor-pointer"
                style={{ color: 'var(--islamic-red)' }}
              >
                <LogOut size={19} />
              </button>
            )}
            <button
              type="button"
              onClick={onDismiss}
              aria-label="إغلاق"
              data-testid="close_dev_dashboard_button"
              className="w-10 h-10 rounded-full flex items-center justify-center cursor-pointer"
              style={{ color: 'var(--islamic-text-secondary)' }}
            >
              <X size={20} />
            </button>
          </div>
        </div>

        {!authState.isLoggedIn ? (
          <form
            onSubmit={handleAuthSubmit}
            className="flex flex-col gap-3.5 overflow-y-auto pt-2"
          >
            <div
              className="rounded-xl border p-4"
              style={{
                backgroundColor: 'var(--islamic-card)',
                borderColor: 'var(--islamic-border)',
              }}
            >
              <h3
                className="font-cairo font-bold text-base mb-1.5"
                style={{ color: 'var(--islamic-gold-light)' }}
              >
                {!authState.isAccountSetup
                  ? 'إنشاء حساب المطوّر الخاص بك'
                  : 'دخول المطوّر المصرح له'}
              </h3>
              <p
                className="font-cairo text-xs leading-relaxed"
                style={{ color: 'var(--islamic-text-secondary)' }}
              >
                {!authState.isAccountSetup
                  ? 'قم بتعيين البريد الإلكتروني وكلمة المرور الخاصة بك. سيتم حفظ البيانات محلياً بشكل مشفّر (SHA-256) دون إرسالها أو كشفها داخل الكود.'
                  : 'أدخل بيانات حسابك للوصول إلى لوحة إدارة القراء، الإشعارات، الإعلانات، والنسخ الاحتياطي.'}
              </p>
            </div>

            <label className="flex flex-col gap-1">
              <span
                className="font-cairo text-xs"
                style={{ color: 'var(--islamic-text-secondary)' }}
              >
                البريد الإلكتروني للمطوّر
              </span>
              <input
                type="email"
                value={inputEmail}
                onChange={(e) => setInputEmail(e.target.value)}
                data-testid="dev_email_input"
                required
                className="w-full rounded-xl border px-3.5 py-2.5 font-cairo text-sm outline-none"
                style={{
                  backgroundColor: 'var(--islamic-card)',
                  borderColor: 'var(--islamic-border)',
                  color: 'var(--islamic-text-primary)',
                }}
              />
            </label>

            <label className="flex flex-col gap-1">
              <span
                className="font-cairo text-xs"
                style={{ color: 'var(--islamic-text-secondary)' }}
              >
                كلمة المرور
              </span>
              <input
                type="password"
                value={inputPassword}
                onChange={(e) => setInputPassword(e.target.value)}
                data-testid="dev_password_input"
                required
                className="w-full rounded-xl border px-3.5 py-2.5 font-cairo text-sm outline-none"
                style={{
                  backgroundColor: 'var(--islamic-card)',
                  borderColor: 'var(--islamic-border)',
                  color: 'var(--islamic-text-primary)',
                }}
              />
            </label>

            {authError && (
              <p
                className="font-cairo text-xs"
                style={{ color: 'var(--islamic-red)' }}
              >
                {authError}
              </p>
            )}

            <button
              type="submit"
              data-testid="dev_submit_auth_button"
              className="w-full h-12 rounded-xl font-cairo font-bold text-[15px] cursor-pointer mt-1"
              style={{
                backgroundColor: 'var(--islamic-gold)',
                color: 'var(--islamic-on-gold)',
              }}
            >
              {!authState.isAccountSetup
                ? 'حفظ وتفعيل حساب المطوّر'
                : 'تسجيل الدخول إلى اللوحة'}
            </button>
          </form>
        ) : (
          <div className="flex flex-col flex-1 min-h-0">
            {/* Scrollable Tabs */}
            <div
              className="flex items-center gap-1 border-b pb-2 mb-4 overflow-x-auto shrink-0"
              style={{ borderColor: 'var(--islamic-border)' }}
            >
              {TABS.map((title, idx) => {
                const active = selectedTabIndex === idx;
                return (
                  <button
                    key={title}
                    type="button"
                    onClick={() => setSelectedTabIndex(idx)}
                    className="px-3 py-1.5 rounded-lg font-cairo text-[13px] whitespace-nowrap shrink-0 cursor-pointer transition-colors"
                    style={{
                      backgroundColor: active
                        ? 'var(--islamic-card-hover)'
                        : 'transparent',
                      color: active
                        ? 'var(--islamic-gold)'
                        : 'var(--islamic-text-secondary)',
                      fontWeight: active ? 700 : 400,
                    }}
                  >
                    {title}
                  </button>
                );
              })}
            </div>

            {/* Tab Body */}
            <div className="flex-1 overflow-y-auto pr-0.5">
              {selectedTabIndex === 0 && (
                <div className="flex flex-col gap-3">
                  <div className="flex items-center justify-between">
                    <span
                      className="font-cairo font-bold text-sm"
                      style={{ color: 'var(--islamic-gold-light)' }}
                    >
                      قائمة القراء ({contentState.recitersList.length})
                    </span>
                    <button
                      type="button"
                      onClick={() => setShowAddForm((v) => !v)}
                      data-testid="add_reciter_button"
                      className="flex items-center gap-1 px-3 py-1.5 rounded-lg font-cairo text-xs font-medium text-white cursor-pointer"
                      style={{ backgroundColor: 'var(--islamic-emerald)' }}
                    >
                      <Plus size={15} />
                      <span>إضافة قارئ جديد</span>
                    </button>
                  </div>

                  {showAddForm && (
                    <div
                      className="rounded-xl border p-3.5 flex flex-col gap-2.5"
                      style={{
                        backgroundColor: 'var(--islamic-card)',
                        borderColor: 'var(--islamic-border)',
                      }}
                    >
                      <span
                        className="font-cairo font-bold text-sm"
                        style={{ color: 'var(--islamic-gold-light)' }}
                      >
                        إضافة قارئ وتلاوة جديدة
                      </span>
                      <input
                        type="text"
                        placeholder="اسم القارئ بالكامل"
                        value={newName}
                        onChange={(e) => setNewName(e.target.value)}
                        className="w-full rounded-lg border px-3 py-2 font-cairo text-xs outline-none"
                        style={{
                          backgroundColor: 'var(--islamic-surface)',
                          borderColor: 'var(--islamic-border)',
                          color: 'var(--islamic-text-primary)',
                        }}
                      />
                      <input
                        type="text"
                        placeholder="الرواية (مثال: حفص عن عاصم)"
                        value={newRiwayah}
                        onChange={(e) => setNewRiwayah(e.target.value)}
                        className="w-full rounded-lg border px-3 py-2 font-cairo text-xs outline-none"
                        style={{
                          backgroundColor: 'var(--islamic-surface)',
                          borderColor: 'var(--islamic-border)',
                          color: 'var(--islamic-text-primary)',
                        }}
                      />
                      <input
                        type="url"
                        dir="ltr"
                        placeholder="https://server10.mp3quran.net/minsh/"
                        value={newServerUrl}
                        onChange={(e) => setNewServerUrl(e.target.value)}
                        className="w-full rounded-lg border px-3 py-2 font-cairo text-xs outline-none"
                        style={{
                          backgroundColor: 'var(--islamic-surface)',
                          borderColor: 'var(--islamic-border)',
                          color: 'var(--islamic-text-primary)',
                        }}
                      />
                      <button
                        type="button"
                        onClick={() => {
                          if (newName.trim() && newServerUrl.trim()) {
                            developerContentManager.addCustomReciter(
                              newName,
                              newRiwayah,
                              newServerUrl,
                            );
                            setNewName('');
                            setShowAddForm(false);
                            onShowToast('تمت إضافة القارئ بنجاح!');
                          }
                        }}
                        className="w-full py-2 rounded-lg font-cairo font-bold text-xs cursor-pointer"
                        style={{
                          backgroundColor: 'var(--islamic-gold)',
                          color: 'var(--islamic-on-gold)',
                        }}
                      >
                        حفظ وإتاحة القارئ
                      </button>
                    </div>
                  )}

                  <div className="flex flex-col gap-2">
                    {contentState.recitersList.map((reciter) => {
                      const isHidden = contentState.hiddenReciterIds.has(
                        reciter.id,
                      );
                      return (
                        <div
                          key={reciter.id}
                          className="rounded-[10px] border p-3 flex items-center justify-between gap-2"
                          style={{
                            backgroundColor: isHidden
                              ? 'var(--islamic-surface)'
                              : 'var(--islamic-card)',
                            borderColor: 'var(--islamic-border)',
                          }}
                        >
                          <div className="min-w-0 flex-1">
                            <div
                              className="font-cairo font-bold text-sm truncate"
                              style={{
                                color: isHidden
                                  ? 'var(--islamic-text-muted)'
                                  : 'var(--islamic-text-primary)',
                              }}
                            >
                              {reciter.nameArabic}
                            </div>
                            <div
                              className="font-cairo text-[11px]"
                              style={{
                                color: isHidden
                                  ? 'var(--islamic-red)'
                                  : 'var(--islamic-emerald-light)',
                              }}
                            >
                              {reciter.riwayah} •{' '}
                              {isHidden ? 'مخفي حالياً' : 'متاح في التطبيق'}
                            </div>
                            <div
                              dir="ltr"
                              className="font-cairo text-[10px] truncate text-right"
                              style={{ color: 'var(--islamic-text-muted)' }}
                            >
                              {reciter.serverBaseUrl}
                            </div>
                          </div>

                          <button
                            type="button"
                            onClick={() =>
                              developerContentManager.toggleHideReciter(
                                reciter.id,
                              )
                            }
                            aria-label={isHidden ? 'إظهار' : 'إخفاء'}
                            className="w-10 h-10 rounded-full flex items-center justify-center shrink-0 cursor-pointer"
                            style={{
                              color: isHidden
                                ? 'var(--islamic-text-muted)'
                                : 'var(--islamic-gold)',
                            }}
                          >
                            {isHidden ? (
                              <EyeOff size={18} />
                            ) : (
                              <Eye size={18} />
                            )}
                          </button>
                        </div>
                      );
                    })}
                  </div>
                </div>
              )}

              {selectedTabIndex === 1 && (
                <div className="flex flex-col gap-3.5">
                  <div
                    className="rounded-xl border p-3.5 flex flex-col gap-2.5"
                    style={{
                      backgroundColor: 'var(--islamic-card)',
                      borderColor: 'var(--islamic-border)',
                    }}
                  >
                    <h3
                      className="font-cairo font-bold text-sm"
                      style={{ color: 'var(--islamic-gold-light)' }}
                    >
                      إرسال إشعار فوري لجميع المستخدمين
                    </h3>
                    <input
                      type="text"
                      value={notifTitle}
                      onChange={(e) => setNotifTitle(e.target.value)}
                      placeholder="عنوان الإشعار"
                      className="w-full rounded-lg border px-3 py-2 font-cairo text-xs outline-none"
                      style={{
                        backgroundColor: 'var(--islamic-surface)',
                        borderColor: 'var(--islamic-border)',
                        color: 'var(--islamic-text-primary)',
                      }}
                    />
                    <textarea
                      rows={3}
                      value={notifMessage}
                      onChange={(e) => setNotifMessage(e.target.value)}
                      placeholder="نص الإشعار"
                      className="w-full rounded-lg border px-3 py-2 font-cairo text-xs outline-none resize-none"
                      style={{
                        backgroundColor: 'var(--islamic-surface)',
                        borderColor: 'var(--islamic-border)',
                        color: 'var(--islamic-text-primary)',
                      }}
                    />
                    <button
                      type="button"
                      onClick={handleSendNotification}
                      data-testid="send_notification_button"
                      className="w-full py-2.5 rounded-lg flex items-center justify-center gap-1.5 font-cairo font-bold text-xs cursor-pointer"
                      style={{
                        backgroundColor: 'var(--islamic-gold)',
                        color: 'var(--islamic-on-gold)',
                      }}
                    >
                      <Bell size={16} />
                      <span>إرسال الإشعار الآن</span>
                    </button>
                  </div>

                  <div
                    className="rounded-xl border p-3.5 flex items-center justify-between gap-3"
                    style={{
                      backgroundColor: 'var(--islamic-card)',
                      borderColor: 'var(--islamic-border)',
                    }}
                  >
                    <div className="min-w-0 flex-1">
                      <div
                        className="font-cairo font-bold text-sm"
                        style={{ color: 'var(--islamic-text-primary)' }}
                      >
                        تذكير سورة الكهف يوم الجمعة
                      </div>
                      <div
                        className="font-cairo text-xs"
                        style={{ color: 'var(--islamic-text-secondary)' }}
                      >
                        إرسال تذكير تلقائي كل جمعة لتلاوة سورة الكهف والاستماع
                        إليها.
                      </div>
                    </div>

                    <button
                      type="button"
                      role="switch"
                      aria-checked={contentState.isFridayReminderEnabled}
                      onClick={() =>
                        developerContentManager.setFridayReminderEnabled(
                          !contentState.isFridayReminderEnabled,
                        )
                      }
                      className="w-12 h-7 rounded-full p-1 transition-colors cursor-pointer shrink-0 flex items-center"
                      style={{
                        backgroundColor: contentState.isFridayReminderEnabled
                          ? 'var(--islamic-emerald)'
                          : 'var(--islamic-surface-variant)',
                        justifyContent: contentState.isFridayReminderEnabled
                          ? 'flex-start'
                          : 'flex-end',
                      }}
                    >
                      <span
                        className="w-5 h-5 rounded-full block"
                        style={{
                          backgroundColor: contentState.isFridayReminderEnabled
                            ? 'var(--islamic-gold)'
                            : 'var(--islamic-text-muted)',
                        }}
                      />
                    </button>
                  </div>
                </div>
              )}

              {selectedTabIndex === 2 && (
                <div className="flex flex-col gap-3.5">
                  <div
                    className="rounded-xl border p-4 flex items-center justify-between gap-3"
                    style={{
                      backgroundColor: 'var(--islamic-card)',
                      borderColor: 'var(--islamic-border)',
                    }}
                  >
                    <div className="min-w-0 flex-1">
                      <div
                        className="font-cairo font-bold text-sm"
                        style={{ color: 'var(--islamic-text-primary)' }}
                      >
                        شريط الإعلانات في أسفل الواجهة (Banner)
                      </div>
                      <div
                        className="font-cairo text-xs"
                        style={{ color: 'var(--islamic-text-secondary)' }}
                      >
                        إظهار أو إخفاء مساحة الإعلان الهادئة أسفل قائمة السور دون
                        التأثير على التلاوة.
                      </div>
                    </div>

                    <button
                      type="button"
                      role="switch"
                      aria-checked={contentState.isBannerEnabled}
                      onClick={() =>
                        developerContentManager.setBannerEnabled(
                          !contentState.isBannerEnabled,
                        )
                      }
                      className="w-12 h-7 rounded-full p-1 transition-colors cursor-pointer shrink-0 flex items-center"
                      style={{
                        backgroundColor: contentState.isBannerEnabled
                          ? 'var(--islamic-emerald)'
                          : 'var(--islamic-surface-variant)',
                        justifyContent: contentState.isBannerEnabled
                          ? 'flex-start'
                          : 'flex-end',
                      }}
                    >
                      <span
                        className="w-5 h-5 rounded-full block"
                        style={{
                          backgroundColor: contentState.isBannerEnabled
                            ? 'var(--islamic-gold)'
                            : 'var(--islamic-text-muted)',
                        }}
                      />
                    </button>
                  </div>

                  <div
                    className="rounded-xl border p-4 flex flex-col gap-1.5"
                    style={{
                      backgroundColor: 'var(--islamic-card)',
                      borderColor: 'var(--islamic-border)',
                    }}
                  >
                    <h3
                      className="font-cairo font-bold text-sm"
                      style={{ color: 'var(--islamic-gold-light)' }}
                    >
                      إعدادات مساحة الإعلان للمطوّر
                    </h3>
                    <p
                      className="font-cairo text-xs leading-relaxed whitespace-pre-line"
                      style={{ color: 'var(--islamic-text-secondary)' }}
                    >
                      {`• لا يتم تشغيل أي إعلان صوتي أو مقاطعة التلاوة في الخلفية مطلقاً.\n• يمكنك إظهار أو إخفاء شريط البانر الهادئ أسفل المشغل في أي وقت.`}
                    </p>
                  </div>
                </div>
              )}

              {selectedTabIndex === 3 && (
                <div className="flex flex-col gap-3.5">
                  <div
                    className="rounded-xl border p-4 flex flex-col gap-3"
                    style={{
                      backgroundColor: 'var(--islamic-card)',
                      borderColor: 'var(--islamic-border)',
                    }}
                  >
                    <div className="flex items-center gap-2">
                      <CloudDownload
                        size={20}
                        style={{ color: 'var(--islamic-gold)' }}
                      />
                      <h3
                        className="font-cairo font-bold text-[15px]"
                        style={{ color: 'var(--islamic-gold-light)' }}
                      >
                        نسخة احتياطية من إعدادات وتلاوات التطبيق
                      </h3>
                    </div>

                    <p
                      className="font-cairo text-xs leading-relaxed"
                      style={{ color: 'var(--islamic-text-secondary)' }}
                    >
                      يمكنك نسخ هذا الملف النصي (JSON) والاحتفاظ به لحفظ كافة
                      بيانات القراء الجدد، الروابط، وإعدادات التطبيق دون الاعتماد
                      على خادم خارجي.
                    </p>

                    <pre
                      dir="ltr"
                      className="w-full max-h-48 overflow-auto rounded-lg border p-2.5 text-[11px] font-mono text-left"
                      style={{
                        backgroundColor: 'var(--islamic-surface)',
                        borderColor: 'var(--islamic-border)',
                        color: 'var(--islamic-text-secondary)',
                      }}
                    >
                      {backupJson}
                    </pre>

                    <button
                      type="button"
                      onClick={handleCopyBackup}
                      data-testid="copy_backup_json_button"
                      className="w-full py-2.5 rounded-lg flex items-center justify-center gap-1.5 font-cairo font-bold text-xs cursor-pointer"
                      style={{
                        backgroundColor: 'var(--islamic-gold)',
                        color: 'var(--islamic-on-gold)',
                      }}
                    >
                      <Copy size={16} />
                      <span>نسخ النسخة الاحتياطية (JSON) للحافظة</span>
                    </button>
                  </div>
                </div>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
