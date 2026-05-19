# Google 로그인 & PRD 배포 가이드

OPP 운영 환경(`https://www.pinksoft.io:8080/opp/`) Google 로그인 연동 및 장애 대응 기록.

## 1. 구성 요약

```
[브라우저] GIS redirect → [Google] → POST credential
    → /opp/auth/google/callback (또는 POST /opp/)
    → GoogleTokenVerifier → AppUserService
    → redirect /opp/?login=username
    → index.html 이 localStorage(opp_user) 저장 후 프로젝트 로드
```

### 관련 파일

| 파일 | 역할 |
|------|------|
| `src/main/resources/static/index.html` | GIS 초기화, `login_uri`, 로그인 UI |
| `GoogleAuthCallbackController.java` | POST `/auth/google/callback` |
| `GoogleRedirectPostFilter.java` | POST `/` (credential) — GET과 충돌 방지용 |
| `GoogleTokenVerifier.java` | ID 토큰 검증 (원격 + 로컬 폴백) |
| `AppUserController.java` | POST `/api/users/google` (popup/직접 API용) |
| `application.yaml` | `google.client-id` |

## 2. Google Cloud Console

**API 및 서비스 → 사용자 인증 정보 → OAuth 2.0 클라이언트 ID (웹 애플리케이션)**

### 승인된 JavaScript 원본

```
https://www.pinksoft.io:8080
https://pinksoft.io:8080
```

### 승인된 리디렉션 URI

```
https://www.pinksoft.io:8080/opp/
https://www.pinksoft.io:8080/opp/auth/google/callback
https://pinksoft.io:8080/opp/
https://pinksoft.io:8080/opp/auth/google/callback
```

- redirect 모드: `login_uri`와 **완전히 동일**해야 함 (끝 `/` 포함)
- 저장 후 전파: **5분 ~ 수 시간**

## 3. PRD 배포

- GitHub: **Actions → Deploy to Tomcat(PRD)** (수동 실행)
- WAR: `/usr/share/tomcat/webapps/opp.war`
- 배포 후: `https://www.pinksoft.io:8080/opp/` 에서 GIS·API 동작 확인

### SSH

```bash
ssh -i ~/.ssh/key_iwinv_a_PINK root@115.68.231.17
```

## 4. iwinv 방화벽 (ELCAP, 서버명 TOMCAT)

### Outbound 필수 규칙

| 서비스 | 프로토콜 | 포트 | 목적지 | 메모 |
|--------|----------|------|--------|------|
| HTTPS | TCP | 443 | 0.0.0.0/0 | Google API |
| DNS | **UDP** | **53** | 0.0.0.0/0 | **필수** — 없으면 호스트명 해석 실패 |

> TCP 443만으로는 부족함. DNS(UDP 53)가 막히면 Java/curl이 `oauth2.googleapis.com`에 연결하지 못함.

### 서버에서 연결 테스트

```bash
# DNS
python3 -c "import socket; print(socket.gethostbyname('oauth2.googleapis.com'))"

# Google API (400 = 연결 성공, 토큰만 잘못된 것)
curl -sS -m 10 -o /dev/null -w "http_code=%{http_code}\n" \
  "https://oauth2.googleapis.com/tokeninfo?id_token=test"
```

DNS 실패 시 IP로 우회 테스트:

```bash
curl -sS -m 10 --resolve "oauth2.googleapis.com:443:74.125.204.95" \
  -o /dev/null -w "http_code=%{http_code}\n" \
  "https://oauth2.googleapis.com/tokeninfo?id_token=test"
```

IP로만 성공하면 **UDP 53 Outbound** 추가.

## 5. 장애 증상별 대응

### COOP / `postMessage` (CORS 아님)

- 메시지: `Cross-Origin-Opener-Policy policy would block the window.postMessage call`
- 원인: GIS **popup** 모드 + 브라우저 COOP
- 대응: **redirect** 모드 사용 (`ux_mode: 'redirect'`). 로그인만 되면 경고 무시 가능.

### `redirect_uri_mismatch`

- Console 리디렉션 URI 등록·전파 대기
- 접속 URL과 등록 URI 일치 (`www` 유무, `:8080`, 끝 `/`)

### `POST /opp/ 405` / `GET /opp/ 405`

- Google redirect는 credential을 **POST**로 보냄
- `GET /opp/ 405`: `@PostMapping("/")` 가 정적 index GET을 가로챈 경우 → Controller에서 제거, Filter만 사용

### `Google 토큰 검증에 실패` / `Google 서버에 연결할 수 없습니다`

- 대부분 **DNS(UDP 53) 차단**
- iwinv Outbound에 UDP 53 추가 후 위 curl 테스트

### API `/api/users/google` 400/500

- 500 + Tomcat NPE: `Map.of("message", e.getMessage())` 에 null → `ApiMessages` 사용
- 배포 WAR가 최신인지 확인

## 6. AI/개발자용 문서 위치

| 위치 | 용도 |
|------|------|
| `.cursor/rules/google-login-deployment.mdc` | Cursor Agent 규칙 (GIS·배포 작업 시) |
| `docs/google-login-deployment.md` | 사람이 읽는 상세 가이드 (이 문서) |
| `README.md` | 프로젝트 진입점에서 링크 |

## 7. 변경 이력 (2026-05)

- GIS redirect + 서버 POST callback
- GoogleTokenVerifier (라이브러리 / tokeninfo / 로컬 JWT 폴백)
- iwinv UDP 53 Outbound 추가로 DNS·Google API 정상화 확인
- 운영 로그인 성공 (`xfile0304` 등)
