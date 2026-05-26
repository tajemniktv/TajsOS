import type { APIRoute } from "astro";

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export const GET: APIRoute = async () =>
	new Response(
		JSON.stringify({ ok: false, error: "Method not allowed. Use POST." }),
		{ status: 405, headers: { "Content-Type": "application/json" } },
	);

export const POST: APIRoute = async ({ request }) => {
	let payload: unknown;

	try {
		payload = await request.json();
	} catch {
		return new Response(
			JSON.stringify({ ok: false, error: "Invalid JSON payload." }),
			{ status: 400, headers: { "Content-Type": "application/json" } },
		);
	}

	const email =
		typeof payload === "object" &&
		payload !== null &&
		"email" in payload &&
		typeof (payload as { email: unknown }).email === "string"
			? (payload as { email: string }).email.trim()
			: "";

	if (!emailPattern.test(email)) {
		return new Response(
			JSON.stringify({ ok: false, error: "Invalid email address." }),
			{ status: 400, headers: { "Content-Type": "application/json" } },
		);
	}

	return new Response(JSON.stringify({ ok: true }), {
		status: 200,
		headers: { "Content-Type": "application/json" },
	});
};
