// jest.config.js
export default {
  // The environment in which the tests should be run
  testEnvironment: 'node',

  // Automatically clear mock calls, instances, contexts and results before every test
  clearMocks: true,

  // A map from regular expressions to paths to transformers
  // This tells Jest to use babel-jest for all .js, .jsx, .ts, and .tsx files
  transform: {
    '^.+\\.(js|jsx|ts|tsx)$': 'babel-jest',
  },

  // An array of file extensions your modules use
  moduleFileExtensions: ['js', 'json', 'jsx', 'node'],

  // The glob patterns Jest uses to detect test files
  testMatch: [
    '**/__tests__/**/*.js?(x)',
    '**/?(*.)+(spec|test).js?(x)',
  ],

  // Indicates whether each individual test should be reported during the run
  verbose: true,
};