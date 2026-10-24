module.exports = {
  testDir: './tests',
  use: { baseURL: 'http://127.0.0.1:8765', launchOptions: process.env.CHROMIUM_PATH ? { executablePath: process.env.CHROMIUM_PATH } : {}, viewport: { width: 390, height: 844 } },
  webServer: { command: 'python3 -m http.server 8765 --bind 127.0.0.1 --directory app/src/main/assets', url: 'http://127.0.0.1:8765', reuseExistingServer: false }
};
