import http from "k6/http";
import { check } from "k6";

const baseUrl = "http://localhost:8080";
const performanceId = 1;
const seatId = 1;

export const options = { vus: 100, duration: "5s" };

export default function () {
  const url = `${baseUrl}/api/performances/${performanceId}/seats/${seatId}/hold`;
  const body = JSON.stringify({ userId: __VU });
  const res = http.post(url, body, { headers: { "Content-Type": "application/json" } });
  check(res, { "200 or 409": (r) => r.status === 200 || r.status === 409 });
}

