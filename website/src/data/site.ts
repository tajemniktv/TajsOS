export const SITE = {
	title: "TajsOS",
	titlePrimary: "Tajs",
	titleHighlight: "OS",
	description:
		"A cognitive operating system designed to curate chaos into clarity.",
	copyright: "© 2026 TajsOS. All systems nominal and all rights reserved.",
	statusText: "System Access Granted",
	waitlistUrl: "waitlist",
	appUrl: "app",
	docsUrl: "overview",
} as const;

export const NAV_LINKS = [
	{ label: "DOCS", href: SITE.docsUrl },
	{ label: "APP", href: SITE.appUrl },
	{ label: "LOCAL-FIRST", href: "local-first" },
	{ label: "FEATURES", href: "features" },
	{ label: "ARCHITECTURE", href: "architecture" },
] as const;

export const FOOTER_LINKS = {
	protocols: [
		{ label: "Privacy", href: "privacy" },
		{ label: "Security", href: "security" },
	],
	network: [
		{ label: "Terms", href: "terms" },
		{ label: "System Status", href: "status" },
	],
	documentation: [
		{ label: "Philosophy", href: "philosophy" },
		{ label: "Changelog", href: "changelog" },
	],
} as const;
