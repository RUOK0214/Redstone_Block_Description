# 검증 기록

검증일: 2026-10-30. 대상: Redstone Help 0.2.0, Minecraft Java 26.2,
Fabric Loader 0.19.5, Fabric API 0.160.0+26.2, Java 25, Windows.

## 결과

- `gradlew --offline build runClientGameTest`: **BUILD SUCCESSFUL**.
- `gradlew --offline runClientGameTest -PcaptureIllustrations`: **BUILD SUCCESSFUL**. 별도 게임 월드에서 640×360 PNG 87장 촬영.
- 전체 148개 등록 아이템의 실제 게임 레지스트리 존재 및 기본·상세 툴팁 연결 확인.
- 한국어·영어 리소스 각각 203개 키, 양쪽 키 일치 확인.
- 한국어·영어 46개 주제 전체에서 추천·평가·활용 예시를 제거하고 기능·작동 조건·수치 위주로 검토.
- 양 언어에서 모든 설명의 번역 키 해석과 최대 250 GUI 픽셀 줄바꿈 검사 통과.
- Shift 누르기 및 해제로 상세·기본 표시 전환 확인.
- 전체 설명 주제의 사진 파일·상태 번역·이미지 렌더러 연결, Shift를 누를 때만 사진 표시 확인.
- 1.8초 간격 슬라이드 전환, 주제 변경·마우스 이탈 후 첫 사진으로 돌아오는 동작 검사 통과.
- 비교기의 비교 모드와 빼기 모드 사진이 실제 화면에서 전환되는 것을 스크린샷으로 확인.
- 촬영 과정에서 전원 출력, 비교기 모드, 피스톤 이동, 호퍼 잠김, 문 열림, 감지체 활성화 등 해당 장면의 상태 검사 통과.
- 사용자 지정 이름과 기존 lore 보존, 숨긴 툴팁 유지, 일반 돌 및 다른 모드 이름 공간 제외 확인.
- 일반 인벤토리·상자·크리에이티브 화면의 표시와 854×480 작은 창의 사진 축소·상세 설명·화면 경계를 스크린샷으로 검토.

## 화면 기록

- [일반 인벤토리](screenshots/inventory-hover-ko.png)
- [Shift 상세 설명](screenshots/inventory-shift-ko.png)
- [자동 전환된 두 번째 사진](screenshots/inventory-slideshow-second-ko.png)
- [레드스톤 가루 설명](screenshots/redstone-dust-shift-ko.png)
- [레버 설명](screenshots/lever-shift-ko.png)
- [854×480 창의 상세 설명](screenshots/inventory-small-shift-ko.png)
- [상자](screenshots/chest-hover-ko.png)
- [크리에이티브 인벤토리](screenshots/creative-hover-ko.png)
- [영어 상세 설명](screenshots/creative-shift-en.png)

테스트는 프로젝트 내부의 별도 월드를 사용했습니다. 상자 화면은 테스트용 컨테이너로 생성해 표시를 검증했습니다.
사용자 게임 프로필에 설치하거나 실제 멀티플레이 서버에 접속하지 않았습니다.
다른 모드가 툴팁 화면을 대체하는 경우의 호환성 및 모든 설명 대상 블록의 모든 회로 동작을 자동 검증한 것은 아닙니다.
사진 속 회로는 촬영한 상태 예시입니다. 전환 간격은 실제 회로의 동작 시간과 다릅니다.

실행 로그에는 개발 계정의 Realms/프로필 인증, Windows 성능 카운터, 기본 그래픽 옵션 관련 경고가 있었으나
테스트를 실패시키거나 도움말 표시를 막지 않았습니다. Gradle의 향후 버전 호환 경고도 빌드를 막지 않았습니다.
