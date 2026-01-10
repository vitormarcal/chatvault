/**
 * Localization and date formatting types
 */

export type SupportedLocale =
  | 'en-US'
  | 'en-GB'
  | 'de-DE'
  | 'fr-FR'
  | 'pt-BR'
  | 'es-ES'
  | 'it-IT'
  | 'ja-JP'
  | 'zh-CN'

export interface FormattedDate {
  formattedDate: string
  isToday: boolean
}

export interface LocalizationState {
  userLocale: SupportedLocale | 'auto'
  systemLocale: SupportedLocale
}

export const LOCALE_DISPLAY_NAMES: Record<SupportedLocale | 'auto', string> = {
  'auto': 'Auto (System Locale)',
  'en-US': 'English (United States)',
  'en-GB': 'English (United Kingdom)',
  'de-DE': 'Deutsch (Deutschland)',
  'fr-FR': 'Français (France)',
  'pt-BR': 'Português (Brasil)',
  'es-ES': 'Español (España)',
  'it-IT': 'Italiano (Italia)',
  'ja-JP': '日本語 (日本)',
  'zh-CN': '简体中文 (中国)',
}

export const SUPPORTED_LOCALES: (SupportedLocale | 'auto')[] = [
  'auto',
  'en-US',
  'en-GB',
  'de-DE',
  'fr-FR',
  'pt-BR',
  'es-ES',
  'it-IT',
  'ja-JP',
  'zh-CN',
]
