Ext.define('Presage2.view.RoadVisualiser', {
	extend: 'Presage2.view.2DVisualiser',
	alias: 'widget.roadvisualiser',
	initComponent: function() {
		Ext.apply(this, {
			xOffset: 200
		});
		this.callParent(arguments);
	},
	getScale: function(simulation) {
		if("length" in simulation.data.parameters) {
			return 500 / simulation.data.parameters["length"];
		} else {
			return 1.0;
		}
	},
	drawAgentSprite: function(ag) {
		var agent = {
			type: 'rect',
			width: this.scale * 0.7,
			height: this.scale,
			fill: '#111',
			x: this.xOffset + (ag.data.data.x * this.scale),
			y: this.yOffset + (ag.data.data.y * this.scale)
		};
		return agent;
	}/*,
	setTimeStep: function(time) {
		var step = this.timeline.getById(time);
		if(step != null) {
			step.agents().each(function(ag) {
				if(ag.getId() in this.sprites) {
					var sp = this.sprites[ag.getId()];
					sp.setAttributes({
						x: 200 + (ag.data.data.x * this.scale),
						y: 10 + (ag.data.data.y * this.scale)
					}, true)
				}
			}, this);
		}
	}*/
});
