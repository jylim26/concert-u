import http from "k6/http";
import { check } from "k6";
import { Counter } from "k6/metrics";

const baseUrl = "http://concert-alb-1837359436.ap-northeast-2.elb.amazonaws.com";
const performanceId = 1;
const seatId = 1;
export const options = {
  scenarios: {
    once: {
      executor: "per-vu-iterations",
      vus: 100,
      iterations: 1,
      maxDuration: "10s",
    },
  },
};
const status200 = new Counter("seat_hold_status_200");
const status409 = new Counter("seat_hold_status_409");
const statusOther = new Counter("seat_hold_status_other");

export default function () {
  const url = `${baseUrl}/api/performances/${performanceId}/seats/${seatId}/hold/pessimistic`;
  const body = JSON.stringify({ userId: __VU });
  const res = http.post(url, body, { headers: { "Content-Type": "application/json" } });

  if (res.status === 200) {
    status200.add(1);
  } else if (res.status === 409) {
    status409.add(1);
  } else {
    statusOther.add(1);
  }

  check(res, { "200 or 409": (r) => r.status === 200 || r.status === 409 });
}
