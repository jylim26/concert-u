import http from "k6/http";
import { check } from "k6";
import { Counter } from "k6/metrics";

const baseUrl = __ENV.BASE_URL || "http://localhost:8080";
const performanceId = Number(__ENV.PERFORMANCE_ID || 1);
const seatStart = Number(__ENV.SEAT_START || 1);
const seatEnd = Number(__ENV.SEAT_END || 30000);
const hotSeatEnd = Number(__ENV.HOT_SEAT_END || 2000);
const hotRatio = Number(__ENV.HOT_RATIO || 0.8);
const rps = Number(__ENV.RPS || 200);
const duration = __ENV.DURATION || "1m";

export const options = {
  scenarios: {
    hold_random_seats: {
      executor: "constant-arrival-rate",
      rate: rps,
      timeUnit: "1s",
      duration: duration,
      preAllocatedVUs: 200,
      maxVUs: 600,
    },
  },
};

const status200 = new Counter("seat_hold_status_200");
const status409 = new Counter("seat_hold_status_409");
const statusOther = new Counter("seat_hold_status_other");

function randomSeatId() {
  if (Math.random() < hotRatio) {
    return Math.floor(Math.random() * (hotSeatEnd - seatStart + 1)) + seatStart;
  }
  return Math.floor(Math.random() * (seatEnd - hotSeatEnd)) + hotSeatEnd + 1;
}

export default function () {
  const seatId = randomSeatId();
  const endpoint = `/api/performances/${performanceId}/seats/${seatId}/hold/optimistic`;
  const body = JSON.stringify({ userId: __VU * 100000 + __ITER });
  const res = http.post(`${baseUrl}${endpoint}`, body, { headers: { "Content-Type": "application/json" } });

  if (res.status === 200) {
    status200.add(1);
  } else if (res.status === 409) {
    status409.add(1);
  } else {
    statusOther.add(1);
  }

  check(res, { "200 or 409": (r) => r.status === 200 || r.status === 409 });
}
