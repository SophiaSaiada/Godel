module.exports = {
  title: "Gödel",
  themeConfig: {
    sidebar: [
      '/',
	  '/types/',
      '/functions/',
	  '/structures/',
	  '/grammar/',
	  '/architecture/',
    ],
	displayAllHeaders: true,
	sidebarDepth: 2,
  },
  markdown: {
    lineNumbers: true
  },
  /*
  base: '/Godel/docs/',
  dest: '../docs/',
  configureWebpack: {
    output: {
	  publicPath: '/Godel/docs/'
	}
  }
  */
}  
